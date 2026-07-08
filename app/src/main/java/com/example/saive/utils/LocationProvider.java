package com.example.saive.utils;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocationProvider {
    private static final String API_BASE_URL = "https://provinces.open-api.vn/api/";
    private static List<Province> provinces;
    private static boolean isLoading = false;
    private static boolean isApiLoaded = false;

    public interface LocationLoadListener {
        void onLoaded();
    }

    public static class Province {
        public int code;
        public String name;
        public List<District> districts;
    }

    public static class District {
        public int code;
        public String name;
        public List<Ward> wards;
    }

    public static class Ward {
        public int code;
        public String name;
    }

    public static void init(Context context) {
        init(context, null);
    }

    private static final List<LocationLoadListener> listeners = new ArrayList<>();

    public static synchronized void init(Context context, LocationLoadListener listener) {
        // Khởi tạo fallback ngay lập tức nếu chưa có dữ liệu để các hàm get không bị rỗng
        if (provinces == null) {
            provinces = getFallbackProvinces();
        }

        if (isLoading) {
            if (listener != null) listeners.add(listener);
            return;
        }

        if (listener != null) {
            listeners.add(listener);
        }
        
        isLoading = true;
        new Thread(() -> {
            try {
                // Tải danh sách tỉnh/thành phố kèm theo quận/huyện và phường/xã (depth=3)
                java.net.URL url = new java.net.URL(API_BASE_URL + "?depth=3");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                InputStream is = conn.getInputStream();
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                String json = s.hasNext() ? s.next() : "";
                is.close();

                JSONArray provinceArray = new JSONArray(json);
                List<Province> tempList = new ArrayList<>();
                for (int i = 0; i < provinceArray.length(); i++) {
                    JSONObject pObj = provinceArray.getJSONObject(i);
                    Province province = new Province();
                    province.name = pObj.getString("name");
                    province.code = pObj.getInt("code");
                    province.districts = new ArrayList<>();
                    
                    if (pObj.has("districts")) {
                        JSONArray districtArray = pObj.getJSONArray("districts");
                        for (int j = 0; j < districtArray.length(); j++) {
                            JSONObject dObj = districtArray.getJSONObject(j);
                            District district = new District();
                            district.name = dObj.getString("name");
                            district.code = dObj.getInt("code");
                            district.wards = new ArrayList<>();
                            
                            if (dObj.has("wards")) {
                                JSONArray wardArray = dObj.getJSONArray("wards");
                                for (int k = 0; k < wardArray.length(); k++) {
                                    JSONObject wObj = wardArray.getJSONObject(k);
                                    Ward ward = new Ward();
                                    ward.name = wObj.getString("name");
                                    ward.code = wObj.getInt("code");
                                    district.wards.add(ward);
                                }
                            }
                            province.districts.add(district);
                        }
                    }
                    tempList.add(province);
                }
                provinces = tempList;
                isApiLoaded = true;
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback nếu lỗi API
                if (provinces == null) {
                    provinces = getFallbackProvinces();
                }
            } finally {
                isLoading = false;
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    synchronized (LocationProvider.class) {
                        for (LocationLoadListener l : listeners) {
                            l.onLoaded();
                        }
                        listeners.clear();
                    }
                });
            }
        }).start();
    }

    private static List<Province> getFallbackProvinces() {
        List<Province> list = new ArrayList<>();
        // Sử dụng tên đầy đủ theo chuẩn API của provinces.open-api.vn
        String[] allProvinces = {
            "Tỉnh An Giang", "Tỉnh Bà Rịa - Vũng Tàu", "Tỉnh Bắc Giang", "Tỉnh Bắc Kạn", "Tỉnh Bạc Liêu", "Tỉnh Bắc Ninh", "Tỉnh Bến Tre", "Tỉnh Bình Định", "Tỉnh Bình Dương", "Tỉnh Bình Phước", "Tỉnh Bình Thuận", "Tỉnh Cà Mau", "Thành phố Cần Thơ", "Tỉnh Cao Bằng", "Thành phố Đà Nẵng", "Tỉnh Đắk Lắk", "Tỉnh Đắk Nông", "Tỉnh Điện Biên", "Tỉnh Đồng Nai", "Tỉnh Đồng Tháp", "Tỉnh Gia Lai", "Tỉnh Hà Giang", "Tỉnh Hà Nam", "Thành phố Hà Nội", "Tỉnh Hà Tĩnh", "Tỉnh Hải Dương", "Thành phố Hải Phòng", "Tỉnh Hậu Giang", "Tỉnh Hòa Bình", "Tỉnh Hưng Yên", "Tỉnh Khánh Hòa", "Tỉnh Kiên Giang", "Tỉnh Kon Tum", "Tỉnh Lai Châu", "Tỉnh Lâm Đồng", "Tỉnh Lạng Sơn", "Tỉnh Lào Cai", "Tỉnh Long An", "Tỉnh Nam Định", "Tỉnh Nghệ An", "Tỉnh Ninh Bình", "Tỉnh Ninh Thuận", "Tỉnh Phú Thọ", "Tỉnh Phú Yên", "Tỉnh Quảng Bình", "Tỉnh Quảng Nam", "Tỉnh Quảng Ngãi", "Tỉnh Quảng Ninh", "Tỉnh Quảng Trị", "Tỉnh Sóc Trăng", "Tỉnh Sơn La", "Tỉnh Tây Ninh", "Tỉnh Thái Bình", "Tỉnh Thái Nguyên", "Tỉnh Thanh Hóa", "Tỉnh Thừa Thiên Huế", "Tỉnh Tiền Giang", "Thành phố Hồ Chí Minh", "Tỉnh Trà Vinh", "Tỉnh Tuyên Quang", "Tỉnh Vĩnh Long", "Tỉnh Vĩnh Phúc", "Tỉnh Yên Bái"
        };
        for (String name : allProvinces) {
            Province p = new Province();
            p.name = name;
            p.districts = new ArrayList<>();
            list.add(p);
        }
        return list;
    }

    public static boolean isLoaded() {
        return isApiLoaded;
    }

    public static boolean isLoading() {
        return isLoading;
    }

    public static List<String> getProvinces(Context context) {
        if (provinces == null || provinces.isEmpty()) {
            // Khởi tạo ngay lập tức với dữ liệu dự phòng để không bị trống
            provinces = getFallbackProvinces();
            init(context); // Vẫn gọi init để cập nhật dữ liệu đầy đủ từ API sau
        }
        
        List<String> list = new ArrayList<>();
        for (Province p : provinces) {
            if (p.name != null) list.add(p.name);
        }
        return list;
    }

    public static List<String> getDistricts(Context context, String provinceName) {
        List<String> list = new ArrayList<>();
        if (provinces != null) {
            for (Province p : provinces) {
                if (p.name != null && p.name.equals(provinceName)) {
                    if (p.districts != null) {
                        for (District d : p.districts) {
                            if (d.name != null) list.add(d.name);
                        }
                    }
                    break;
                }
            }
        }
        return list;
    }

    public static List<String> getWards(Context context, String provinceName, String districtName) {
        List<String> list = new ArrayList<>();
        if (provinces != null) {
            for (Province p : provinces) {
                if (p.name.equals(provinceName)) {
                    for (District d : p.districts) {
                        if (d.name.equals(districtName)) {
                            for (Ward w : d.wards) list.add(w.name);
                            break;
                        }
                    }
                    break;
                }
            }
        }
        return list;
    }
}
