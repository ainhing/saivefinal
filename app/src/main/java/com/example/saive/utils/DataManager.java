package com.example.saive.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.saive.models.Coupon;
import com.example.saive.models.Product;
import com.example.saive.models.AdminOrder;
import com.example.saive.models.User;
import com.example.saive.models.Review;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String PREF_NAME = "saive_data_prefs";
    private static final String KEY_COUPONS = "coupons";
    private static final String KEY_PRODUCTS = "products";
    private static final String KEY_ORDERS = "orders";
    private static final String KEY_USERS = "users";
    private static final String KEY_REVIEWS = "reviews";
    private static final String KEY_FLASH_SALE = "flash_sale";
    private static final String KEY_PAYMENT_CARDS = "payment_cards";

    private static final String KEY_DATA_VERSION = "data_version";
    private static final int CURRENT_DATA_VERSION = 3; // Version 3 for multi-item orders demo

    private static DataManager instance;
    private Context context;
    private Gson gson;
    private SharedPreferences prefs;

    // Memory Cache
    private List<Coupon> cachedCoupons;
    private List<Product> cachedProducts;
    private List<AdminOrder> cachedOrders;
    private String currentCachedUserId; // Thêm để quản lý cache theo user
    private List<User> cachedUsers;
    private List<Review> cachedReviews;
    private List<Product> cachedFlashSaleProducts;
    private List<com.example.saive.models.PaymentCard> cachedPaymentCards;

    private DataManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
            instance.startListeningReviews();
            instance.startListeningProducts(); // Bắt đầu lắng nghe thay đổi sản phẩm & kho từ Firebase
            instance.startListeningOrders();
            instance.startListeningVouchers();
            instance.startListeningUsers();
        }
        return instance;
    }

    // Clear cache if needed (e.g., on logout)
    public void clearCache() {
        cachedCoupons = null;
        cachedProducts = null;
        cachedOrders = null;
        currentCachedUserId = null;
        cachedUsers = null;
        cachedReviews = null;
        cachedFlashSaleProducts = null;
        cachedPaymentCards = null;
    }

    /**
     * Xóa sạch dữ liệu cá nhân của người dùng khỏi Disk (SharedPreferences) và RAM.
     * Gọi khi đăng xuất.
     */
    public void clearAllUserData() {
        // 1. Clear RAM
        clearCache();

        // 2. Clear Disk (SharedPreferences)
        // Lưu ý: Chỉ xóa dữ liệu nhạy cảm/cá nhân. 
        // Giữ lại 'products' và 'coupons' vì đó là dữ liệu chung của hệ thống.
        SharedPreferences.Editor editor = prefs.edit();
        
        // Xóa danh sách thẻ
        editor.remove(KEY_PAYMENT_CARDS);
        
        // Xóa đơn hàng (vì getOrdersKey dùng suffix userId, ta cần tìm tất cả các key liên quan hoặc đơn giản là xóa prefix)
        // Tuy nhiên SharedPreferences không hỗ trợ xóa theo prefix dễ dàng. 
        // Cách an toàn nhất cho demo này là clear các key chính.
        editor.remove(KEY_ORDERS);
        
        // Nếu có các key order dạng orders_U001, orders_U002... 
        // ta nên dùng clear() nếu file này chỉ chứa dữ liệu user, 
        // nhưng file này chứa cả KEY_PRODUCTS, KEY_COUPONS.
        // Vì vậy ta sẽ duyệt qua tất cả keys để xóa những cái bắt đầu bằng KEY_ORDERS.
        java.util.Map<String, ?> allEntries = prefs.getAll();
        for (String key : allEntries.keySet()) {
            if (key.startsWith(KEY_ORDERS)) {
                editor.remove(key);
            }
        }

        editor.apply();
    }

    // --- Coupons ---
    public List<Coupon> getCoupons() {
        if (cachedCoupons != null) return new ArrayList<>(cachedCoupons);
        
        String json = prefs.getString(KEY_COUPONS, null);
        if (json == null) {
            return new ArrayList<>(); // Trả về list trống, fetch từ server sau
        }
        Type type = new TypeToken<ArrayList<Coupon>>() {}.getType();
        cachedCoupons = gson.fromJson(json, type);
        return cachedCoupons != null ? new ArrayList<>(cachedCoupons) : new ArrayList<>();
    }

    public void saveCoupons(List<Coupon> coupons) {
        cachedCoupons = coupons != null ? new ArrayList<>(coupons) : null;
        prefs.edit().putString(KEY_COUPONS, gson.toJson(coupons)).apply();
    }

    public void addCoupon(Coupon coupon) {
        List<Coupon> coupons = getCoupons();
        coupons.add(0, coupon);
        saveCoupons(coupons);
    }

    // --- Products ---
    private com.google.firebase.database.ValueEventListener productsListener;

    public void startListeningProducts() {
        if (productsListener != null) return;

        com.google.firebase.database.DatabaseReference rootRef = 
            com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference();
        
        // Fetch Categories first to map ID to Name
        rootRef.child("Categories").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot catSnapshot) {
                java.util.Map<String, String> categoryMap = new java.util.HashMap<>();
                for (com.google.firebase.database.DataSnapshot child : catSnapshot.getChildren()) {
                    String id = getStringSafe(child.child("CategoryId"));
                    String name = getStringSafe(child.child("CategoryName"));
                    if (!id.isEmpty() && !name.isEmpty()) {
                        categoryMap.put(id, name.trim().toLowerCase());
                    }
                }

                // Now listen to products
                productsListener = new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        List<Product> products = new ArrayList<>();
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            try {
                                String name = getStringSafe(child.child("ProductName"));
                                if (name.isEmpty()) continue;

                                String catId = getStringSafe(child.child("CategoryId"));
                                String catName = categoryMap.get(catId);
                                if (catName == null) catName = catId != null ? catId.toLowerCase() : "other";

                                Product p = new Product(name, String.valueOf(child.child("Price").getValue()), 0, catName);
                                p.setProductId(child.getKey());
                                p.setDescription(getStringSafe(child.child("Description")));
                                p.setNameEn(getStringSafe(child.child("ProductName_en")));
                                p.setNameZh(getStringSafe(child.child("ProductName_zh")));
                                p.setDescriptionEn(getStringSafe(child.child("Description_en")));
                                p.setDescriptionZh(getStringSafe(child.child("Description_zh")));

                                // Read tags
                                String tagTypeGroup = getStringSafe(child.child("tag_type_group"));
                                if (tagTypeGroup.isEmpty()) tagTypeGroup = getStringSafe(child.child("TagTypeGroup"));
                                if (tagTypeGroup.isEmpty()) p.setTagTypeGroup(tagTypeGroup.trim().toLowerCase());

                                String tagStyle = getStringSafe(child.child("tag_style"));
                                if (tagStyle.isEmpty()) p.setTagStyle(tagStyle.trim().toLowerCase());

                                String tagType = getStringSafe(child.child("tag_type"));
                                if (tagType.isEmpty()) p.setTagType(tagType.trim().toLowerCase());

                                java.util.List<String> tagColorList = new java.util.ArrayList<>();
                                com.google.firebase.database.DataSnapshot tagColorSnap = child.child("tag_color");
                                if (tagColorSnap.exists()) {
                                    for (com.google.firebase.database.DataSnapshot colorChild : tagColorSnap.getChildren()) {
                                        String c = getStringSafe(colorChild);
                                        if (!c.isEmpty()) tagColorList.add(c.trim().toLowerCase());
                                    }
                                }
                                if (!tagColorList.isEmpty()) p.setTagColor(tagColorList);
                                
                                // Cập nhật số lượng tổng
                                Integer sq = child.child("StockQuantity").getValue(Integer.class);
                                p.setStockQuantity(sq != null ? sq : 0);
                                
                                // Load variantsStock from Firebase (Variants or Stock node)
                                java.util.Map<String, java.util.Map<String, Integer>> variantsStock = new java.util.HashMap<>();
                                com.google.firebase.database.DataSnapshot variantsSnap = child.child("Variants");
                                if (!variantsSnap.exists()) variantsSnap = child.child("Stock");
                                
                                if (variantsSnap.exists()) {
                                    for (com.google.firebase.database.DataSnapshot variantSnap : variantsSnap.getChildren()) {
                                        String key = variantSnap.getKey();
                                        if (key == null) continue;
                                        
                                        if (key.contains("_")) {
                                            // Format: {size}_{color} (e.g., M_Black)
                                            String[] parts = key.split("_");
                                            if (parts.length == 2) {
                                                String size = parts[0];
                                                String color = parts[1];
                                                Object s = variantSnap.child("Stock").getValue();
                                                if (s == null) s = variantSnap.getValue();
                                                
                                                if (s instanceof Number) {
                                                    java.util.Map<String, Integer> colors = variantsStock.get(size);
                                                    if (colors == null) {
                                                        colors = new java.util.HashMap<>();
                                                        variantsStock.put(size, colors);
                                                    }
                                                    colors.put(color, ((Number) s).intValue());
                                                }
                                            }
                                        } else {
                                            // Legacy Format: {size}/{color}
                                            String size = key;
                                            java.util.Map<String, Integer> colors = variantsStock.get(size);
                                            if (colors == null) {
                                                colors = new java.util.HashMap<>();
                                                variantsStock.put(size, colors);
                                            }
                                            for (com.google.firebase.database.DataSnapshot colorSnap : variantSnap.getChildren()) {
                                                String color = colorSnap.getKey();
                                                Object s = colorSnap.child("Stock").getValue();
                                                if (s == null) s = colorSnap.getValue();
                                                if (s instanceof Number) {
                                                    colors.put(color, ((Number) s).intValue());
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!variantsStock.isEmpty()) p.setVariantsStock(variantsStock);

                                // Ánh xạ danh sách ảnh
                                java.util.List<String> images = new ArrayList<>();
                                com.google.firebase.database.DataSnapshot imgsSnap = child.child("Images");
                                if (imgsSnap.exists()) {
                                    for (com.google.firebase.database.DataSnapshot img : imgsSnap.getChildren()) {
                                        String url = getStringSafe(img);
                                        if (url.isEmpty()) images.add(url);
                                    }
                                }
                                
                                // Fallback to imageUrl/imageUrls fields used by Admin panel
                                if (images.isEmpty()) {
                                    com.google.firebase.database.DataSnapshot urlsSnap = child.child("imageUrls");
                                    if (!urlsSnap.exists()) urlsSnap = child.child("ImageUrls");
                                    
                                    if (urlsSnap.exists()) {
                                        for (com.google.firebase.database.DataSnapshot img : urlsSnap.getChildren()) {
                                            String url = getStringSafe(img);
                                            if (url.isEmpty()) images.add(url);
                                        }
                                    }
                                }
                                
                                String firstImg = "";
                                if (!images.isEmpty()) {
                                    firstImg = images.get(0);
                                } else {
                                    com.google.firebase.database.DataSnapshot singleUrlSnap = child.child("imageUrl");
                                    if (!singleUrlSnap.exists()) singleUrlSnap = child.child("ImageUrl");
                                    if (singleUrlSnap.exists()) {
                                        firstImg = getStringSafe(singleUrlSnap);
                                        if (firstImg != null && !firstImg.isEmpty()) images.add(firstImg);
                                    }
                                }

                                p.setImageUrls(images);
                                p.setImageUrl(firstImg);

                                products.add(p);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        saveProducts(products);
                        notifyProductListeners();
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
                };
                rootRef.child("Products").addValueEventListener(productsListener);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }


    public List<Product> getProducts() {
        if (cachedProducts != null) return new ArrayList<>(cachedProducts);
        
        String json = prefs.getString(KEY_PRODUCTS, null);
        if (json == null) {
            return new ArrayList<>(); // Trả về list trống, MainActivity sẽ fetch từ server
        }
        Type type = new TypeToken<ArrayList<Product>>() {}.getType();
        cachedProducts = gson.fromJson(json, type);
        if (cachedProducts != null) {
            // Fix Gson bug: Map<String, Integer> gets deserialized as Map<String, Double>
            // Normalize all stock values back to Integer
            for (Product p : cachedProducts) {
                normalizeVariantsStock(p);
            }
        }
        return cachedProducts != null ? new ArrayList<>(cachedProducts) : new ArrayList<>();
    }

    /**
     * Gson deserializes Map<String, Integer> as Map<String, Double> because of type erasure.
     * This method rebuilds the variantsStock map with proper Integer values.
     */
    private void normalizeVariantsStock(Product p) {
        if (p == null || p.getVariantsStock() == null) return;
        java.util.Map<String, java.util.Map<String, Integer>> normalized = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, java.util.Map<String, Integer>> sizeEntry : p.getVariantsStock().entrySet()) {
            java.util.Map<String, Integer> colorMapNorm = new java.util.HashMap<>();
            if (sizeEntry.getValue() != null) {
                for (java.util.Map.Entry<String, Integer> colorEntry : sizeEntry.getValue().entrySet()) {
                    Object val = colorEntry.getValue();
                    int intVal = 0;
                    if (val instanceof Number) intVal = ((Number) val).intValue();
                    else if (val != null) { try { intVal = Integer.parseInt(val.toString()); } catch (Exception ignored) {} }
                    colorMapNorm.put(colorEntry.getKey(), intVal);
                }
            }
            normalized.put(sizeEntry.getKey(), colorMapNorm);
        }
        p.setVariantsStock(normalized);
    }

    public void saveProducts(List<Product> products) {
        cachedProducts = products != null ? new ArrayList<>(products) : null;
        prefs.edit().putString(KEY_PRODUCTS, gson.toJson(products)).apply();
        generateAndSaveFlashSale(products);
    }

    /**
     * Tự động tạo danh sách Flash Sale từ danh sách sản phẩm chính.
     * Logic này được chuyển từ MainActivity sang để đảm bảo dữ liệu luôn đồng bộ.
     */
    public void generateAndSaveFlashSale(List<Product> allProducts) {
        if (allProducts == null || allProducts.isEmpty()) return;

        List<Product> tempFlash = new ArrayList<>();
        
        // 1. Ưu tiên lấy sản phẩm được đánh dấu là Featured
        for (Product p : allProducts) {
            if (p.isFeatured()) {
                Product flashProduct = createFlashProduct(p, 0.3); // Giảm 30%
                if (flashProduct != null) tempFlash.add(flashProduct);
            }
            if (tempFlash.size() >= 10) break;
        }

        // 2. Nếu không đủ 10 sản phẩm, lấy ngẫu nhiên thêm
        if (tempFlash.size() < 5) {
            List<Product> shuffleList = new ArrayList<>(allProducts);
            java.util.Collections.shuffle(shuffleList);
            for (Product p : shuffleList) {
                if (tempFlash.size() >= 10) break;
                
                // Kiểm tra xem sản phẩm đã có trong list flash chưa
                boolean alreadyIn = false;
                for (Product existing : tempFlash) {
                    if (existing.getName().equals(p.getName())) {
                        alreadyIn = true;
                        break;
                    }
                }
                
                if (!alreadyIn) {
                    Product flashProduct = createFlashProduct(p, 0.2); // Giảm 20%
                    if (flashProduct != null) tempFlash.add(flashProduct);
                }
            }
        }

        saveFlashSaleProducts(tempFlash);
    }

    private Product createFlashProduct(Product p, double discount) {
        try {
            double priceVal = com.example.saive.utils.PriceFormatter.parsePrice(p.getPrice());
            if (priceVal <= 0) return null;
            
            double salePriceVal = priceVal * (1 - discount);

            String salePrice = String.format(java.util.Locale.US, "%.0f", salePriceVal);
            String originalPrice = String.format(java.util.Locale.US, "%.0f", priceVal);

            Product flashProduct = new Product(p.getName(), salePrice, originalPrice,
                    p.getImageResId(), p.getCategory(), p.getDescription());
            flashProduct.setProductId(p.getProductId());
            flashProduct.setImageUrl(p.getImageUrl());
            flashProduct.setImageUrls(p.getImageUrls());
            flashProduct.setTagTypeGroup(p.getTagTypeGroup());
            flashProduct.setFeatured(p.isFeatured());
            return flashProduct;
        } catch (Exception e) {
            return null;
        }
    }


    // --- Orders ---
    private String getOrdersKey(String userId) {
        return KEY_ORDERS + (userId == null || userId.isEmpty() ? "" : "_" + userId);
    }

    public List<AdminOrder> getOrders(String userId) {
        // Kiểm tra RAM cache trước
        if (cachedOrders != null && userId != null && userId.equals(currentCachedUserId)) {
            return new ArrayList<>(cachedOrders);
        }

        String key = getOrdersKey(userId);
        String json = prefs.getString(key, null);
        
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<ArrayList<AdminOrder>>() {}.getType();
        List<AdminOrder> orders = gson.fromJson(json, type);

        // Cập nhật RAM cache
        if (orders != null) {
            cachedOrders = new ArrayList<>(orders);
            currentCachedUserId = userId;
        }

        return orders != null ? new ArrayList<>(orders) : new ArrayList<>();
    }

    public void saveOrders(List<AdminOrder> orders, String userId) {
        cachedOrders = orders != null ? new ArrayList<>(orders) : null;
        currentCachedUserId = userId;
        prefs.edit().putString(getOrdersKey(userId), gson.toJson(orders)).apply();
    }

    public void addOrder(AdminOrder order, String userId) {
        List<AdminOrder> orders = getOrders(userId);
        orders.add(0, order);
        saveOrders(orders, userId);
    }

    public AdminOrder getOrderById(String orderId, String userId) {
        List<AdminOrder> orders = getOrders(userId);
        for (AdminOrder order : orders) {
            String existingId = order.getOrderId();
            if (existingId != null) {
                if (existingId.equals(orderId) || 
                    existingId.equals("#" + orderId) ||
                    orderId.equals("#" + existingId)) {
                    return order;
                }
            }
        }
        return null;
    }

    public void updateOrderStatus(String orderId, String newStatus, String userId) {
        List<AdminOrder> orders = getOrders(userId);
        for (AdminOrder order : orders) {
            if (order.getOrderId().equals(orderId)) {
                order.setStatus(newStatus);
                break;
            }
        }
        saveOrders(orders, userId);
        cachedOrders = null; // invalidate cache
    }

    public void updateOrderId(String oldOrderId, String newOrderId, String userId) {
        List<AdminOrder> orders = getOrders(userId);
        for (AdminOrder order : orders) {
            if (order.getOrderId().equals(oldOrderId)) {
                order.setOrderId(newOrderId);
                break;
            }
        }
        saveOrders(orders, userId);
        cachedOrders = null; // invalidate cache
    }

    // --- Users ---
    public List<User> getUsers() {
        if (cachedUsers != null) return new ArrayList<>(cachedUsers);
        
        String json = prefs.getString(KEY_USERS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<User>>() {}.getType();
        cachedUsers = gson.fromJson(json, type);
        return cachedUsers != null ? new ArrayList<>(cachedUsers) : new ArrayList<>();
    }

    public void saveUsers(List<User> users) {
        cachedUsers = users != null ? new ArrayList<>(users) : null;
        prefs.edit().putString(KEY_USERS, gson.toJson(users)).apply();
    }

    public void setUserBlocked(String userEmail, boolean isBlocked) {
        if (userEmail == null) return;
        List<User> users = getUsers();
        for (User user : users) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(userEmail)) {
                user.setBlocked(isBlocked);
                break;
            }
        }
        saveUsers(users);
    }
    
    public boolean isUserBlocked(String email) {
        if (email == null) return false;
        List<User> users = getUsers();
        for (User user : users) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
                return user.isBlocked();
            }
        }
        return false;
    }

    private List<OnReviewChangeListener> reviewListeners = new ArrayList<>();
    private List<OnProductChangeListener> productListeners = new ArrayList<>();

    public interface OnReviewChangeListener {
        void onReviewsChanged();
    }

    public interface OnProductChangeListener {
        void onProductsChanged();
    }

    public void addReviewListener(OnReviewChangeListener listener) {
        if (!reviewListeners.contains(listener)) {
            reviewListeners.add(listener);
        }
    }

    public void removeReviewListener(OnReviewChangeListener listener) {
        reviewListeners.remove(listener);
    }

    public void addProductListener(OnProductChangeListener listener) {
        if (!productListeners.contains(listener)) {
            productListeners.add(listener);
        }
    }

    public void removeProductListener(OnProductChangeListener listener) {
        productListeners.remove(listener);
    }

    private void notifyReviewListeners() {
        for (OnReviewChangeListener listener : reviewListeners) {
            listener.onReviewsChanged();
        }
    }

    private void notifyProductListeners() {
        for (OnProductChangeListener listener : productListeners) {
            listener.onProductsChanged();
        }
    }

    // --- Reviews ---
    private com.google.firebase.database.ValueEventListener ordersListener;
    private com.google.firebase.database.ValueEventListener orderDetailsListener;
    private com.google.firebase.database.ValueEventListener vouchersListener;
    private com.google.firebase.database.ValueEventListener usersListener;
    private com.google.firebase.database.DataSnapshot lastOrdersSnapshot;
    private com.google.firebase.database.DataSnapshot lastOrderDetailsSnapshot;

    private com.google.firebase.database.ValueEventListener reviewsListener;

    public void startListeningReviews() {
        if (reviewsListener != null) return;

        com.google.firebase.database.DatabaseReference rootRef =
                com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference();

        // Bước 1: Lấy danh sách Users để map CustomerId -> DisplayName
        rootRef.child("Users").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot usersSnapshot) {
                java.util.Map<String, String> userNameMap = new java.util.HashMap<>();
                for (com.google.firebase.database.DataSnapshot userChild : usersSnapshot.getChildren()) {
                    String displayName = getStringSafe(userChild.child("DisplayName"));
                    if (!displayName.isEmpty() && userChild.getKey() != null) {
                        userNameMap.put(userChild.getKey(), displayName);
                    }
                }

                // Bước 2: Lắng nghe Reviews, dùng userNameMap để resolve tên
                com.google.firebase.database.DatabaseReference reviewsRef = rootRef.child("Reviews");
                reviewsListener = new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        List<Review> reviews = new ArrayList<>();
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            try {
                                Object approvedObj = child.child("IsApproved").getValue();
                                if (approvedObj == null) approvedObj = child.child("isApproved").getValue();
                                if (approvedObj == null) approvedObj = child.child("Is_Approved").getValue();
                                if (approvedObj == null) approvedObj = child.child("is_approved").getValue();

                                boolean isApproved = false;
                                if (approvedObj instanceof Boolean) {
                                    isApproved = (Boolean) approvedObj;
                                } else if (approvedObj instanceof String) {
                                    isApproved = "true".equalsIgnoreCase((String) approvedObj);
                                } else if (approvedObj instanceof Number) {
                                    isApproved = ((Number) approvedObj).intValue() == 1;
                                }

                                String status = getStringSafe(child.child("Status"));
                                if (status.isEmpty()) status = getStringSafe(child.child("status"));
                                if ("approved".equalsIgnoreCase(status)) isApproved = true;

                                if (isApproved) {
                                    String pId = getStringSafe(child.child("ProductId"));
                                    if (pId.isEmpty()) pId = getStringSafe(child.child("productId"));

                                    String pName = getStringSafe(child.child("ProductName"));
                                    if (pName.isEmpty()) pName = getStringSafe(child.child("productName"));
                                    if (pName.isEmpty()) pName = pId;

                                    // Ưu tiên UserName ghi trực tiếp trên review
                                    String uName = getStringSafe(child.child("UserName"));
                                    if (uName.isEmpty()) uName = getStringSafe(child.child("userName"));

                                    // Nếu không có, tra CustomerId -> DisplayName qua userNameMap
                                    if (uName.isEmpty()) {
                                        String customerId = getStringSafe(child.child("CustomerId"));
                                        if (customerId.isEmpty()) customerId = getStringSafe(child.child("customerId"));

                                        if (!customerId.isEmpty()) {
                                            String resolvedName = userNameMap.get(customerId);
                                            uName = (resolvedName != null && !resolvedName.isEmpty()) ? resolvedName : customerId;
                                        }
                                    }
                                    if (uName.isEmpty()) uName = "Anonymous";

                                    Object ratingObj = child.child("Rating").getValue();
                                    if (ratingObj == null) ratingObj = child.child("rating").getValue();

                                    float rating = 0f;
                                    if (ratingObj instanceof Number) {
                                        rating = ((Number) ratingObj).floatValue();
                                    } else if (ratingObj instanceof String) {
                                        try { rating = Float.parseFloat((String) ratingObj); } catch (Exception ignored) {}
                                    }

                                    String comment = getStringSafe(child.child("Comment"));
                                    if (comment.isEmpty()) comment = getStringSafe(child.child("comment"));
                                    if (comment.isEmpty()) comment = getStringSafe(child.child("Content"));
                                    if (comment.isEmpty()) comment = getStringSafe(child.child("content"));

                                    String date = getStringSafe(child.child("Date"));
                                    if (date.isEmpty()) date = getStringSafe(child.child("date"));
                                    if (date.isEmpty()) date = getStringSafe(child.child("Created_at"));
                                    if (date.isEmpty()) date = getStringSafe(child.child("created_at"));
                                    if (date.isEmpty()) date = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(new java.util.Date());

                                    List<String> images = new ArrayList<>();
                                    com.google.firebase.database.DataSnapshot imgsSnap = child.child("Images");
                                    if (!imgsSnap.exists()) imgsSnap = child.child("images");
                                    if (!imgsSnap.exists()) imgsSnap = child.child("imageUrls");

                                    if (imgsSnap.exists()) {
                                        for (com.google.firebase.database.DataSnapshot img : imgsSnap.getChildren()) {
                                            String url = getStringSafe(img);
                                            if (url != null) images.add(url);
                                        }
                                    }

                                    Review r = new Review(pName, uName, rating, comment, date, images);
                                    r.setProductId(pId);
                                    r.setApproved(true);
                                    reviews.add(r);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        saveReviews(reviews);
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
                };
                reviewsRef.addValueEventListener(reviewsListener);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    public void startListeningOrders() {
        if (ordersListener != null || orderDetailsListener != null) return;

        com.google.firebase.database.DatabaseReference rootRef =
                com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference();

        // Lắng nghe OrderDetails realtime
        orderDetailsListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                lastOrderDetailsSnapshot = snapshot;
                rebuildOrdersAndSave();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
        };
        rootRef.child("OrderDetails").addValueEventListener(orderDetailsListener);

        // Lắng nghe Orders realtime
        ordersListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                lastOrdersSnapshot = snapshot;
                rebuildOrdersAndSave();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
        };
        rootRef.child("Orders").addValueEventListener(ordersListener);
    }

    /**
     * Ghép lastOrderDetailsSnapshot + lastOrdersSnapshot thành danh sách AdminOrder theo từng userId,
     * rồi lưu xuống disk. Được gọi lại mỗi khi 1 trong 2 node đổi (realtime).
     */
    private void rebuildOrdersAndSave() {
        if (lastOrderDetailsSnapshot == null || lastOrdersSnapshot == null) return;

        try {
            // Bước 1: Gom item theo OrderId từ OrderDetails
            java.util.Map<String, List<com.example.saive.models.OrderItem>> itemsByOrder = new java.util.HashMap<>();

            for (com.google.firebase.database.DataSnapshot child : lastOrderDetailsSnapshot.getChildren()) {
                try {
                    String orderId = getStringSafe(child.child("OrderId"));
                    if (orderId.isEmpty()) continue;

                    String productName = getStringSafe(child.child("ProductName"));
                    String size = getStringSafe(child.child("Size"));
                    String color = getStringSafe(child.child("Color"));

                    Object qtyObj = child.child("Quantity").getValue();
                    int quantity = 1;
                    if (qtyObj instanceof Number) quantity = ((Number) qtyObj).intValue();

                    Object priceObj = child.child("UnitPrice").getValue();
                    double unitPrice = 0;
                    if (priceObj instanceof Number) unitPrice = ((Number) priceObj).doubleValue();
                    String priceStr = String.format(java.util.Locale.US, "%.0f", unitPrice);

                    String imageUrl = getStringSafe(child.child("Image"));

                    com.example.saive.models.OrderItem item = new com.example.saive.models.OrderItem(
                            productName,
                            size.isEmpty() ? "M" : size,
                            color.isEmpty() ? null : color,
                            quantity,
                            priceStr,
                            0,
                            imageUrl.isEmpty() ? null : imageUrl
                    );

                    List<com.example.saive.models.OrderItem> list = itemsByOrder.get(orderId);
                    if (list == null) {
                        list = new ArrayList<>();
                        itemsByOrder.put(orderId, list);
                    }
                    list.add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Bước 2: Ghép vào Orders, gom theo UserId
            java.util.Map<String, List<AdminOrder>> ordersByUser = new java.util.HashMap<>();

            for (com.google.firebase.database.DataSnapshot child : lastOrdersSnapshot.getChildren()) {
                try {
                    String orderId = child.getKey();
                    if (orderId == null) continue;

                    String userId = getStringSafe(child.child("UserId"));
                    if (userId.isEmpty()) userId = getStringSafe(child.child("CustomerId"));
                    if (userId.isEmpty()) continue;

                    String fullName = getStringSafe(child.child("FullName"));
                    String status = getStringSafe(child.child("Status"));
                    String paymentMethod = getStringSafe(child.child("PaymentMethod"));
                    String shippingAddress = getStringSafe(child.child("ShippingAddress"));
                    String createdAt = getStringSafe(child.child("CreatedAt"));

                    Object totalObj = child.child("TotalAmount").getValue();
                    double totalAmount = 0;
                    if (totalObj instanceof Number) totalAmount = ((Number) totalObj).doubleValue();
                    String totalStr = String.format(java.util.Locale.US, "%.0f", totalAmount);

                    List<com.example.saive.models.OrderItem> items = itemsByOrder.get(orderId);
                    String itemsSummary;
                    if (items != null && !items.isEmpty()) {
                        itemsSummary = items.get(0).getName()
                                + (items.size() > 1 ? " +" + (items.size() - 1) : "");
                    } else {
                        itemsSummary = "Order";
                    }

                    AdminOrder order = new AdminOrder(
                            orderId,
                            fullName.isEmpty() ? userId : fullName,
                            itemsSummary,
                            totalStr,
                            AdminOrder.normalizeStatus(status),
                            createdAt
                    );
                    order.setPaymentMethod(paymentMethod.isEmpty() ? "COD" : paymentMethod);
                    order.setShippingAddress(shippingAddress);
                    if (items != null) order.setItems(items);

                    List<AdminOrder> list = ordersByUser.get(userId);
                    if (list == null) {
                        list = new ArrayList<>();
                        ordersByUser.put(userId, list);
                    }
                    list.add(order);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (java.util.Map.Entry<String, List<AdminOrder>> entry : ordersByUser.entrySet()) {
                saveOrdersRaw(entry.getValue(), entry.getKey());
            }
            cachedOrders = null;
            currentCachedUserId = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Ghi list order xuống disk cho đúng userId, không đụng vào RAM cache (tránh cache user khác bị ghi đè). */
    private void saveOrdersRaw(List<AdminOrder> orders, String userId) {
        prefs.edit().putString(getOrdersKey(userId), gson.toJson(orders)).apply();
    }

    // --- Vouchers -> Coupons (sync từ Firebase) ---

    public void startListeningVouchers() {
        if (vouchersListener != null) return;

        com.google.firebase.database.DatabaseReference ref =
                com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference("Vouchers");

        vouchersListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                List<Coupon> coupons = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    try {
                        // Data trên Firebase có 2 kiểu tên field (chữ hoa/thường lẫn lộn) -> check cả 2
                        String code = getStringSafe(child.child("code"));
                        if (code.isEmpty()) code = getStringSafe(child.child("Code"));
                        if (code.isEmpty()) code = child.getKey();

                        String title = getStringSafe(child.child("title"));
                        if (title.isEmpty()) title = getStringSafe(child.child("Title"));

                        String description = getStringSafe(child.child("description"));
                        if (description.isEmpty()) description = getStringSafe(child.child("Description"));

                        String discount = getStringSafe(child.child("discount"));
                        if (discount.isEmpty()) discount = getStringSafe(child.child("Discount"));

                        String expiryDate = getStringSafe(child.child("endDate"));
                        if (expiryDate.isEmpty()) expiryDate = getStringSafe(child.child("EndDate"));

                        String status = getStringSafe(child.child("status"));
                        if (status.isEmpty()) status = getStringSafe(child.child("Status"));
                        if (status.isEmpty()) status = "Active";

                        String type = getStringSafe(child.child("type"));
                        if (type.isEmpty()) type = getStringSafe(child.child("Type"));
                        if (type.isEmpty()) type = getStringSafe(child.child("discountType"));
                        if (type.isEmpty()) type = "Percentage";

                        Object usedObj = child.child("usedCount").getValue();
                        if (usedObj == null) usedObj = child.child("UsedCount").getValue();
                        int usageCount = 0;
                        if (usedObj instanceof Number) usageCount = ((Number) usedObj).intValue();

                        Coupon coupon = new Coupon(title, description, discount, expiryDate, code, status, usageCount, type);
                        coupons.add(coupon);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                saveCoupons(coupons);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
        };
        ref.addValueEventListener(vouchersListener);
    }

    // --- Users (sync từ Firebase) ---

    public void startListeningUsers() {
        if (usersListener != null) return;

        com.google.firebase.database.DatabaseReference ref =
                com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference("Users");

        usersListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    try {
                        String id = child.getKey();
                        if (id == null) continue;

                        String name = getStringSafe(child.child("DisplayName"));
                        if (name.isEmpty()) name = getStringSafe(child.child("FullName"));
                        if (name.isEmpty()) name = id;

                        String email = getStringSafe(child.child("Email"));

                        String role = getStringSafe(child.child("Role"));
                        if (role.isEmpty()) role = "customer";

                        String avatarUrl = getStringSafe(child.child("AvatarUrl"));

                        User user = new User(id, name, email, role, avatarUrl.isEmpty() ? null : avatarUrl);

                        Object activeObj = child.child("IsActive").getValue();
                        boolean isActive = true;
                        if (activeObj instanceof Boolean) isActive = (Boolean) activeObj;
                        else if (activeObj instanceof String) isActive = "true".equalsIgnoreCase((String) activeObj);
                        user.setBlocked(!isActive);

                        users.add(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                saveUsers(users);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
        };
        ref.addValueEventListener(usersListener);
    }

    public List<Review> getReviews() {
        if (cachedReviews != null) return new ArrayList<>(cachedReviews);
        
        String json = prefs.getString(KEY_REVIEWS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Review>>() {}.getType();
        cachedReviews = gson.fromJson(json, type);
        return cachedReviews != null ? new ArrayList<>(cachedReviews) : new ArrayList<>();
    }

    public void saveReviews(List<Review> reviews) {
        cachedReviews = reviews != null ? new ArrayList<>(reviews) : null;
        prefs.edit().putString(KEY_REVIEWS, gson.toJson(reviews)).apply();
        notifyReviewListeners();
    }

    public void addReview(Review review) {
        List<Review> reviews = getReviews();
        reviews.add(0, review);
        saveReviews(reviews);
    }

    public boolean hasPurchasedProduct(String userId, String productName) {
        // ALWAYS RETURN TRUE FOR TESTING PURPOSES TO VERIFY UI
        // return true; 

        if (userId == null || userId.isEmpty() || productName == null) return false;
        
        final String targetName = productName.trim().toLowerCase().replaceAll("\\s+", "");
        List<AdminOrder> orders = getOrders(userId);
        
        for (AdminOrder order : orders) {
            String status = order.getStatus() != null ? order.getStatus().toUpperCase(java.util.Locale.ROOT) : "";
            if (status.equals("COMPLETED") || status.equals("DELIVERED") || status.equals("SHIPPED")) {
                if (order.getItems() != null) {
                    for (com.example.saive.models.OrderItem item : order.getItems()) {
                        String itemName = item.getName() != null ? 
                                item.getName().trim().toLowerCase().replaceAll("\\s+", "") : "";
                        if (targetName.equals(itemName)) {
                            return true;
                        }
                    }
                }
                
                String summary = order.getItemsSummary() != null ? 
                        order.getItemsSummary().trim().toLowerCase().replaceAll("\\s+", "") : "";
                if (targetName.equals(summary)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void submitReviewToFirebase(Review review, Runnable onSuccess, Runnable onFailure) {
        com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase.getInstance("https://saive-403f7-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Reviews");

        ref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                int maxId = 0;
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key != null && key.startsWith("RV")) {
                        try {
                            int idNum = Integer.parseInt(key.substring(2));
                            if (idNum > maxId) {
                                maxId = idNum;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                String newReviewId = String.format(java.util.Locale.US, "RV%03d", maxId + 1);

                java.util.Map<String, Object> reviewMap = new java.util.HashMap<>();
                reviewMap.put("ProductName", review.getProductName());
                reviewMap.put("ProductId", review.getProductId()); // Added ProductId
                reviewMap.put("UserName", review.getUserName());
                reviewMap.put("Rating", review.getRating());
                reviewMap.put("Comment", review.getComment());
                reviewMap.put("Date", review.getDate());
                reviewMap.put("IsApproved", false);
                reviewMap.put("Status", "pending"); // Added Status field

                ref.child(newReviewId).setValue(reviewMap)
                        .addOnSuccessListener(aVoid -> {
                            addReview(review); // Add to local cache too
                            if (onSuccess != null) onSuccess.run();
                        })
                        .addOnFailureListener(e -> {
                            if (onFailure != null) onFailure.run();
                        });
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                if (onFailure != null) onFailure.run();
            }
        });
    }

    // --- Flash Sale ---
    private static final String KEY_FLASH_SALE_END_TIME = "flash_sale_end_time";
    private static final String KEY_FLASH_SALE_PRODUCTS = "flash_sale_products";

    public void setFlashSaleEndTime(long endTimeMillis) {
        prefs.edit().putLong(KEY_FLASH_SALE_END_TIME, endTimeMillis).apply();
    }

    public long getFlashSaleEndTime() {
        return prefs.getLong(KEY_FLASH_SALE_END_TIME, 0);
    }

    public List<Product> getFlashSaleProducts() {
        if (cachedFlashSaleProducts != null) return new ArrayList<>(cachedFlashSaleProducts);
        
        String json = prefs.getString(KEY_FLASH_SALE_PRODUCTS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Product>>() {}.getType();
        cachedFlashSaleProducts = gson.fromJson(json, type);
        return cachedFlashSaleProducts != null ? new ArrayList<>(cachedFlashSaleProducts) : new ArrayList<>();
    }

    public void saveFlashSaleProducts(List<Product> products) {
        cachedFlashSaleProducts = products != null ? new ArrayList<>(products) : null;
        prefs.edit().putString(KEY_FLASH_SALE_PRODUCTS, gson.toJson(products)).apply();
    }

    public void setFlashSale(String productId, double discountPercent, long endTimeMillis) {
        prefs.edit().putString(KEY_FLASH_SALE + "_" + productId, discountPercent + ":" + endTimeMillis).apply();
    }

    public String getFlashSale(String productId) {
        return prefs.getString(KEY_FLASH_SALE + "_" + productId, null);
    }

    // --- Payment Cards ---
    public List<com.example.saive.models.PaymentCard> getPaymentCards() {
        if (cachedPaymentCards != null) return new ArrayList<>(cachedPaymentCards);
        
        String json = prefs.getString(KEY_PAYMENT_CARDS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<com.example.saive.models.PaymentCard>>() {}.getType();
        cachedPaymentCards = gson.fromJson(json, type);
        return cachedPaymentCards != null ? new ArrayList<>(cachedPaymentCards) : new ArrayList<>();
    }

    public void savePaymentCards(List<com.example.saive.models.PaymentCard> cards) {
        cachedPaymentCards = cards != null ? new ArrayList<>(cards) : null;
        prefs.edit().putString(KEY_PAYMENT_CARDS, gson.toJson(cards)).apply();
    }

    public void addPaymentCard(com.example.saive.models.PaymentCard card) {
        List<com.example.saive.models.PaymentCard> cards = getPaymentCards();
        cards.add(card);
        savePaymentCards(cards);
    }

    public static String getStringSafe(com.google.firebase.database.DataSnapshot snapshot) {
        if (snapshot == null) return "";
        Object val = snapshot.getValue();
        return (val == null) ? "" : String.valueOf(val);
    }

}
