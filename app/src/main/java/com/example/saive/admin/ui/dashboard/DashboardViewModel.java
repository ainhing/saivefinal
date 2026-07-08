package com.example.saive.admin.ui.dashboard;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.connectors.FirebaseConnector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import org.json.JSONObject;
import org.json.JSONArray;

public class DashboardViewModel extends AndroidViewModel {
    private final MutableLiveData<Map<String, Object>> stats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Map<String, Object>> getStats() { return stats; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadStats() {
        isLoading.setValue(true);
        Map<String, Object> currentStats = new HashMap<>();

        // Use a counter to know when all main nodes are fetched
        // Nodes: Users, Products, Orders, Blogs, Reviews
        final int[] nodesToFetch = {5};

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nodeName = snapshot.getRef().getKey();
                if ("Users".equals(nodeName)) {
                    currentStats.put("totalUsers", snapshot.getChildrenCount());
                } else if ("Products".equals(nodeName)) {
                    long activeCount = 0;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Boolean isActive = child.child("IsActive").getValue(Boolean.class);
                        if (isActive == null || isActive) activeCount++; // Fallback to true if null
                    }
                    currentStats.put("totalProducts", activeCount);
                } else if ("Orders".equals(nodeName)) {
                    currentStats.put("totalOrders", snapshot.getChildrenCount());
                    double revenue = 0;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String status = child.child("Status").getValue(String.class);
                        if (status == null) {
                            status = child.child("status").getValue(String.class);
                        }
                        
                        Object totalObj = child.child("TotalAmount").getValue();
                        if (totalObj == null) {
                            totalObj = child.child("totalAmount").getValue();
                        }
                        
                        double total = 0;
                        if (totalObj instanceof Number) {
                            total = ((Number) totalObj).doubleValue();
                        } else if (totalObj instanceof String) {
                            try {
                                total = Double.parseDouble((String) totalObj);
                            } catch (Exception e) {
                                total = 0.0;
                            }
                        }
                        
                        if (status != null && (status.equalsIgnoreCase("delivered") 
                                || status.equalsIgnoreCase("deliveried") 
                                || status.equalsIgnoreCase("Đã giao"))) {
                            revenue += total;
                        }
                    }
                    currentStats.put("totalRevenue", revenue);
                } else if ("Blogs".equals(nodeName)) {
                    currentStats.put("totalBlogs", snapshot.getChildrenCount());
                } else if ("Reviews".equals(nodeName)) {
                    currentStats.put("totalReviews", snapshot.getChildrenCount());
                    
                    long prodCount = 0;
                    long priceCount = 0;
                    long servCount = 0;
                    long delivCount = 0;

                    // Word Cloud and Treemap computation
                    Map<String, Integer> wordFreq = new HashMap<>();
                    
                    // Stopwords set
                    Set<String> stopWords = new HashSet<>(Arrays.asList(
                        "và", "có", "là", "thì", "mà", "của", "được", "cho", "trong", "ra", "lại", "ở", "bị", "cái", "này", "như", "nó", "đây", "đó", 
                        "với", "các", "những", "một", "nhưng", "cũng", "đã", "đang", "sẽ", "đi", "về", "lên", "xuống", "vào", "đến", 
                        "nhiều", "ít", "quá", "rất", "khá", "để", "nếu", "khi", "vì", "nên", "tự", "mình", "họ", "ta", "tôi", "bạn", 
                        "nha", "ạ", "nhé", "nhe", "ấy", "đều", "hơn", "nhất", "không", "ko", "k", "khg", "chưa", "được", "rồi", "nữa",
                        "cực", "kỳ", "thật", "luôn", "thấy", "lại", "còn", "chỉ", "sự", "việc", "sản", "phẩm", "sp", "hàng", "cửa", "hàng"
                    ));

                    // Subtopics counts
                    long prodQuality = 0, prodFeatures = 0, prodDesign = 0, prodPerformance = 0, prodFit = 0;
                    long priceCost = 0, priceValue = 0, pricePromo = 0, priceCompare = 0;
                    long servShop = 0, servConsult = 0, servSupport = 0, servAttitude = 0, servResponse = 0;
                    long delivSpeed = 0, delivShipper = 0, delivPkg = 0, delivStatus = 0, delivPolicy = 0, delivFee = 0, delivQty = 0, delivTarget = 0;

                    for (DataSnapshot child : snapshot.getChildren()) {
                        String content = child.child("Content").getValue(String.class);
                        if (content == null) content = child.child("Comment").getValue(String.class);
                        if (content == null) content = child.child("content").getValue(String.class);
                        if (content == null) content = child.child("comment").getValue(String.class);

                        if (content != null && !content.trim().isEmpty()) {
                            String text = content.toLowerCase(java.util.Locale.getDefault());
                            
                            // Check for subtopic keywords
                            boolean hasProd = false;
                            if (text.contains("chất lượng") || text.contains("bền") || text.contains("dễ hỏng") || text.contains("chắc chắn") || text.contains("mỏng manh") || text.contains("vải") || text.contains("chất vải") || text.contains("xịn") || text.contains("nát") || text.contains("hỏng") || text.contains("rách") || text.contains("yếu")) {
                                prodQuality++; hasProd = true;
                            }
                            if (text.contains("tính năng") || text.contains("chức năng") || text.contains("hoạt động") || text.contains("đầy đủ") || text.contains("thiếu") || text.contains("sử dụng") || text.contains("tiện lợi")) {
                                prodFeatures++; hasProd = true;
                            }
                            if (text.contains("đẹp") || text.contains("xấu") || text.contains("màu") || text.contains("màu sắc") || text.contains("kiểu dáng") || text.contains("kích thước") || text.contains("size") || text.contains("mẫu mã") || text.contains("thiết kế") || text.contains("form") || text.contains("dáng")) {
                                prodDesign++; hasProd = true;
                            }
                            if (text.contains("nhanh") || text.contains("chậm") || text.contains("mạnh") || text.contains("yếu") || text.contains("tiêu thụ điện") || text.contains("pin") || text.contains("nóng") || text.contains("mượt")) {
                                if (!text.contains("giao") && !text.contains("ship") && !text.contains("vận chuyển")) {
                                    prodPerformance++; hasProd = true;
                                }
                            }
                            if (text.contains("mô tả") || text.contains("vừa") || text.contains("vừa vặn") || text.contains("rộng") || text.contains("chật") || text.contains("phù hợp") || text.contains("đúng mô tả") || text.contains("y hình") || text.contains("giống ảnh")) {
                                prodFit++; hasProd = true;
                            }
                            if (hasProd) prodCount++;

                            // PRICE subtopics
                            boolean hasPrice = false;
                            if (text.contains("giá") || text.contains("đắt") || text.contains("rẻ") || text.contains("tiền") || text.contains("chi phí") || text.contains("thấp") || text.contains("cao") || text.contains("tầm giá") || text.contains("giá cả") || text.contains("hạt dẻ")) {
                                priceCost++; hasPrice = true;
                            }
                            if (text.contains("đáng tiền") || text.contains("xứng đáng") || text.contains("đáng mua") || text.contains("worth") || text.contains("tiết kiệm")) {
                                priceValue++; hasPrice = true;
                            }
                            if (text.contains("khuyến mãi") || text.contains("giảm giá") || text.contains("deal") || text.contains("voucher") || text.contains("sale") || text.contains("quà") || text.contains("tặng")) {
                                pricePromo++; hasPrice = true;
                            }
                            if (text.contains("cạnh tranh") || text.contains("rẻ hơn")) {
                                priceCompare++; hasPrice = true;
                            }
                            if (hasPrice) priceCount++;

                            // SERVICE subtopics
                            boolean hasServ = false;
                            if (text.contains("shop") || text.contains("người bán") || text.contains("chủ shop") || text.contains("cửa hàng") || text.contains("gian hàng")) {
                                servShop++; hasServ = true;
                            }
                            if (text.contains("tư vấn") || text.contains("giải đáp") || text.contains("hướng dẫn")) {
                                servConsult++; hasServ = true;
                            }
                            if (text.contains("hỗ trợ") || text.contains("giải quyết") || text.contains("chăm sóc")) {
                                servSupport++; hasServ = true;
                            }
                            if (text.contains("thân thiện") || text.contains("lịch sự") || text.contains("chuyên nghiệp") || text.contains("nhiệt tình") || text.contains("dễ thương") || text.contains("chu đáo")) {
                                servAttitude++; hasServ = true;
                            }
                            if (text.contains("phản hồi") || text.contains("trả lời") || text.contains("nhanh") || text.contains("rep") || text.contains("tin nhắn")) {
                                servResponse++; hasServ = true;
                            }
                            if (hasServ) servCount++;

                            // DELIVERY subtopics
                            boolean hasDeliv = false;
                            if (text.contains("giao") || text.contains("ship") || text.contains("vận chuyển") || text.contains("nhanh") || text.contains("chậm") || text.contains("trễ") || text.contains("lâu") || text.contains("đúng hẹn") || text.contains("tốc độ")) {
                                if (text.contains("giao") || text.contains("ship") || text.contains("vận chuyển")) {
                                    delivSpeed++; hasDeliv = true;
                                }
                            }
                            if (text.contains("shipper") || text.contains("giao tận nơi") || text.contains("bác tài") || text.contains("anh ship") || text.contains("người giao")) {
                                delivShipper++; hasDeliv = true;
                            }
                            if (text.contains("đóng gói") || text.contains("hộp") || text.contains("bọc") || text.contains("gói") || text.contains("bao bì") || text.contains("chống sốc") || text.contains("kỹ") || text.contains("cẩn thận")) {
                                delivPkg++; hasDeliv = true;
                            }
                            if (text.contains("nguyên vẹn") || text.contains("móp") || text.contains("méo") || text.contains("hư hại") || text.contains("nát") || text.contains("vỡ") || text.contains("bể")) {
                                delivStatus++; hasDeliv = true;
                            }
                            if (text.contains("đổi trả") || text.contains("hoàn tiền")) {
                                delivPolicy++; hasDeliv = true;
                            }
                            if (text.contains("phí ship") || text.contains("tiền ship") || text.contains("ship mắc") || text.contains("free ship") || text.contains("miễn phí ship")) {
                                delivFee++; hasDeliv = true;
                            }
                            if (text.contains("thiếu") || text.contains("đủ") || text.contains("thiếu hàng") || text.contains("đủ số lượng")) {
                                delivQty++; hasDeliv = true;
                            }
                            if (text.contains("đúng") || text.contains("sai") || text.contains("nhầm") || text.contains("sai hàng") || text.contains("nhầm hàng") || text.contains("đúng mẫu") || text.contains("sai mẫu")) {
                                delivTarget++; hasDeliv = true;
                            }
                            if (hasDeliv) delivCount++;

                            // Word frequencies for Word Cloud
                            String cleanText = text.replaceAll("[\\p{Punct}&&[^_]]", " ");
                            String[] words = cleanText.split("\\s+");
                            for (String w : words) {
                                w = w.trim();
                                if (w.length() > 2 && !stopWords.contains(w) && !w.matches("\\d+")) {
                                    wordFreq.put(w, wordFreq.getOrDefault(w, 0) + 1);
                                }
                            }
                        }
                    }

                    // Build Word Cloud JSON Array
                    List<Map.Entry<String, Integer>> wordList = new ArrayList<>(wordFreq.entrySet());
                    Collections.sort(wordList, (a, b) -> b.getValue().compareTo(a.getValue()));
                    
                    JSONArray wcArray = new JSONArray();
                    int maxWords = Math.min(wordList.size(), 40);
                    for (int i = 0; i < maxWords; i++) {
                        Map.Entry<String, Integer> entry = wordList.get(i);
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("name", entry.getKey());
                            obj.put("value", entry.getValue());
                            wcArray.put(obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    currentStats.put("wordCloudJson", wcArray.toString());

                    // Build Treemap JSON Array
                    JSONArray tmArray = new JSONArray();
                    try {
                        JSONObject prodTopic = new JSONObject();
                        prodTopic.put("name", "PRODUCT");
                        prodTopic.put("value", prodQuality + prodFeatures + prodDesign + prodPerformance + prodFit);
                        JSONArray prodKids = new JSONArray();
                        prodKids.put(createKidObj("Quality (Chất lượng)", prodQuality));
                        prodKids.put(createKidObj("Features (Tính năng)", prodFeatures));
                        prodKids.put(createKidObj("Design (Thiết kế)", prodDesign));
                        prodKids.put(createKidObj("Performance (Hiệu năng)", prodPerformance));
                        prodKids.put(createKidObj("Fit (Độ phù hợp)", prodFit));
                        prodTopic.put("children", prodKids);
                        tmArray.put(prodTopic);

                        JSONObject priceTopic = new JSONObject();
                        priceTopic.put("name", "PRICE");
                        priceTopic.put("value", priceCost + priceValue + pricePromo + priceCompare);
                        JSONArray priceKids = new JSONArray();
                        priceKids.put(createKidObj("Price (Giá cả)", priceCost));
                        priceKids.put(createKidObj("Value (Giá trị)", priceValue));
                        priceKids.put(createKidObj("Promos (Khuyến mãi)", pricePromo));
                        priceKids.put(createKidObj("Compare (So sánh)", priceCompare));
                        priceTopic.put("children", priceKids);
                        tmArray.put(priceTopic);

                        JSONObject servTopic = new JSONObject();
                        servTopic.put("name", "SERVICE");
                        servTopic.put("value", servShop + servConsult + servSupport + servAttitude + servResponse);
                        JSONArray servKids = new JSONArray();
                        servKids.put(createKidObj("Shop (Cửa hàng)", servShop));
                        servKids.put(createKidObj("Consult (Tư vấn)", servConsult));
                        servKids.put(createKidObj("Support (Hỗ trợ)", servSupport));
                        servKids.put(createKidObj("Attitude (Thái độ)", servAttitude));
                        servKids.put(createKidObj("Response (Phản hồi)", servResponse));
                        servTopic.put("children", servKids);
                        tmArray.put(servTopic);

                        JSONObject delivTopic = new JSONObject();
                        delivTopic.put("name", "DELIVERY");
                        delivTopic.put("value", delivSpeed + delivShipper + delivPkg + delivStatus + delivPolicy + delivFee + delivQty + delivTarget);
                        JSONArray delivKids = new JSONArray();
                        delivKids.put(createKidObj("Speed (Tốc độ)", delivSpeed));
                        delivKids.put(createKidObj("Shipper (Người giao)", delivShipper));
                        delivKids.put(createKidObj("Packaging (Đóng gói)", delivPkg));
                        delivKids.put(createKidObj("Status (Tình trạng)", delivStatus));
                        delivKids.put(createKidObj("Policy (Chính sách)", delivPolicy));
                        delivKids.put(createKidObj("Delivery fee (Phí ship)", delivFee));
                        delivKids.put(createKidObj("Quantity (Số lượng)", delivQty));
                        delivKids.put(createKidObj("Target (Đối tượng)", delivTarget));
                        delivTopic.put("children", delivKids);
                        tmArray.put(delivTopic);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    currentStats.put("treemapJson", tmArray.toString());
                    
                    currentStats.put("reviewProductCount", prodCount);
                    currentStats.put("reviewPriceCount", priceCount);
                    currentStats.put("reviewServiceCount", servCount);
                    currentStats.put("reviewDeliveryCount", delivCount);
                }

                nodesToFetch[0]--;
                if (nodesToFetch[0] <= 0) {
                    stats.setValue(currentStats);
                    isLoading.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                nodesToFetch[0]--;
                if (nodesToFetch[0] <= 0) {
                    isLoading.setValue(false);
                }
                errorMessage.setValue(error.getMessage());
            }
        };

        FirebaseConnector.getDatabase().getReference("Users").addListenerForSingleValueEvent(listener);
        FirebaseConnector.getDatabase().getReference("Products").addListenerForSingleValueEvent(listener);
        FirebaseConnector.getDatabase().getReference("Orders").addListenerForSingleValueEvent(listener);
        FirebaseConnector.getDatabase().getReference("Blogs").addListenerForSingleValueEvent(listener);
        FirebaseConnector.getDatabase().getReference("Reviews").addListenerForSingleValueEvent(listener);
    }

    private static JSONObject createKidObj(String name, long value) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("value", value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}