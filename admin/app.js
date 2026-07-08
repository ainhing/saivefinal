/* =====================================================
   SAIVE Admin Dashboard — Core Logic
   Quiet Luxury Branding & Firebase RTDB Sync
   ===================================================== */

'use strict';

const $ = id => document.getElementById(id);

// --- i18n Support ---
const i18n = {
  vi: {
    "login-system-title": "Hệ thống Quản trị",
    "login-email-label": "Email Quản trị",
    "login-password-label": "Mật khẩu",
    "login-btn": "Đăng nhập",
    "login-error-msg": "Thông tin đăng nhập không chính xác!",
    "sidebar-admin-panel": "Admin Panel",
    "nav-dashboard": "Dashboard",
    "nav-products": "Sản phẩm",
    "nav-orders": "Đơn hàng",
    "nav-users": "Người dùng",
    "nav-employees": "Nhân viên",
    "nav-blogs": "Blog",
    "nav-coupons": "Voucher",
    "nav-reviews": "Đánh giá",
    "nav-settings": "Cài đặt",
    "status-connecting": "Đang kết nối...",
    "status-online": "Đã kết nối Firebase",
    "status-offline": "Mất kết nối...",
    "btn-refresh": " Làm mới",
    "btn-logout": "Đăng xuất",
    "stat-products": "Sản phẩm",
    "stat-orders": "Đơn hàng",
    "stat-users": "Người dùng",
    "stat-blogs": "Blog",
    "stat-reviews": "Đánh giá",
    "stat-revenue": "Tổng doanh thu",
    "card-revenue-growth": " Tăng trưởng doanh thu (7 ngày gần nhất)",
    "card-recent-orders": " Đơn hàng gần đây",
    "card-order-stats": " Thống kê đơn hàng",
    "label-by-status": "Theo trạng thái",
    "label-popular-categories": "Danh mục phổ biến",
    "th-order-id": "Mã đơn",
    "th-customer": "Khách hàng",
    "th-items": "Sản phẩm",
    "th-total": "Tổng tiền",
    "th-status": "Trạng thái",
    "th-email": "Email",
    "th-payment": "PT thanh toán",
    "th-date": "Ngày tạo",
    "th-image": "Ảnh",
    "th-id": "ID",
    "th-product-name": "Tên sản phẩm",
    "th-price": "Giá",
    "th-stock": "Tồn kho",
    "th-category": "Danh mục",
    "th-rating": "Đánh giá",
    "th-action": "Thao tác",
    "th-avatar": "Avatar",
    "th-name": "Tên",
    "th-dob": "Ngày sinh",
    "th-gender": "Giới tính",
    "th-role": "Vai trò",
    "th-fullname": "Họ tên",
    "th-position": "Vai trò",
    "th-shift": "Ca làm",
    "th-cover": "Ảnh bìa",
    "th-title": "Tiêu đề",
    "th-author": "Tác giả",
    "th-tags": "Tags",
    "th-product": "Sản phẩm",
    "th-user": "Người dùng",
    "th-review": "Đánh giá",
    "th-content": "Nội dung",
    "settings-title": "️ Cài đặt hệ thống & Giao diện",
    "label-shop-name": "Tên cửa hàng",
    "label-language": "Ngôn ngữ Dashboard",
    "label-theme": "Chế độ hiển thị",
    "label-shipping-fee": "Phí vận chuyển (đ)",
    "label-min-freeship": "Đơn hàng tối thiểu Freeship (đ)",
    "label-address": "Địa chỉ văn phòng",
    "label-phone": "Số điện thoại hỗ trợ",
    "label-contact-email": "Email liên hệ",
    "btn-save-settings": "Lưu cấu hình",
    "placeholder-search-products": " Tìm sản phẩm...",
    "placeholder-search-orders": "Tìm đơn hàng...",
    "placeholder-search-users": " Tìm người dùng...",
    "placeholder-search-employees": "Tìm nhân viên...",
    "placeholder-search-blogs": " Tìm blog...",
    "placeholder-search-coupons": " Tìm voucher...",
    "placeholder-search-reviews": " Tìm đánh giá...",
    "btn-add-product": "+ Thêm sản phẩm",
    "btn-add-user": "+ Thêm người dùng",
    "btn-add-employee": "+ Thêm nhân viên",
    "btn-add-blog": "+ Thêm blog",
    "btn-add-coupon": "+ Thêm Voucher",
    "opt-all-status": "Tất cả trạng thái",
    "opt-pending": "Chờ xác nhận",
    "opt-confirmed": "Đã xác nhận",
    "opt-shipping": "Đang giao",
    "opt-delivered": "Đã giao",
    "opt-cancelled": "Đã hủy",
    "opt-active": "Hoạt động",
    "opt-expired": "Hết hạn",
    "opt-scheduled": "Đã lên lịch",
    "opt-all": "Tất cả",
    "opt-approved": "Đã duyệt",
    "opt-pending-review": "Chờ duyệt",
    "toast-welcome-admin": "Chào mừng Admin trở lại!",
    "toast-no-permission": "Tài khoản này không có quyền truy cập Admin!",
    "toast-incorrect-pass": "Mật khẩu không chính xác!",
    "toast-email-not-found": "Email không tồn tại trong hệ thống!",
    "toast-refreshing": "Đang làm mới dữ liệu từ Firebase...",
    "toast-error-conn": "Lỗi kết nối Firebase!",
    "confirm-logout": "Bạn có chắc chắn muốn đăng xuất?",
    "confirm-delete": "Xác nhận xóa dữ liệu này?",
    "card-review-topics": " Phân bổ chủ đề đánh giá",
    "label-analysis-note": "Dựa trên phân tích từ khóa nội dung (AI Analysis)",
    "modal-product-title": "Thêm sản phẩm",
    "modal-order-detail-title": "Chi tiết đơn hàng",
    "modal-add-order-title": "Thêm đơn hàng mới",
    "modal-user-title": "Người dùng",
    "modal-employee-title": "Quản lý nhân viên",
    "modal-blog-title": "Bài viết",
    "modal-coupon-title": "Quản lý Voucher",
    "th-coupon-code": "Mã Code",
    "th-discount": "Giảm giá",
    "th-expiry": "Hết hạn",
    "th-usage": "Lượt dùng",
    "label-coupon-code": "Mã Voucher (Code) *",
    "label-coupon-title": "Tiêu đề Voucher *",
    "label-coupon-discount": "Giá trị giảm (VNĐ hoặc %)",
    "label-coupon-expiry": "Ngày hết hạn (dd/MM/yyyy)",
    "label-product-id": "Product ID *",
    "label-category-id": "Danh mục ID",
    "opt-select-category": "-- Chọn danh mục --",
    "label-product-name": "Tên sản phẩm *",
    "label-original-price": "Giá gốc (VNĐ)",
    "label-price-vnd": "Giá bán (VNĐ)",
    "label-stock": "Tồn kho",
    "label-active": "Kích hoạt",
    "label-featured": "Nổi bật",
    "label-img-url": "URL Ảnh (mỗi dòng một URL)",
    "label-description": "Mô tả",
    "btn-cancel": "Hủy",
    "btn-save": " Lưu",
    "btn-close": "Đóng",
    "btn-update": "Cập nhật",
    "label-update-status": "Cập nhật trạng thái:",
    "label-customer-name": "Khách hàng *",
    "label-phone-short": "SĐT *",
    "label-shipping-address": "Địa chỉ giao hàng *",
    "label-order-items": "Sản phẩm (Ví dụ: 1x Áo thun (M) | 1x Quần jean (32)) *",
    "label-total-vnd": "Tổng tiền (VNĐ) *",
    "label-payment-method": "PT Thanh toán",
    "label-note": "Ghi chú",
    "label-dob-format": "Ngày sinh (dd/MM/yyyy)",
    "opt-male": "Nam",
    "opt-female": "Nữ",
    "opt-other": "Khác",
    "opt-no-reveal": "Không muốn tiết lộ",
    "opt-active": "Hoạt động",
    "opt-locked": "Bị khóa",
    "label-position": "Chức vụ",
    "label-department": "Phòng ban",
    "label-blog-title": "Tiêu đề *",
    "label-blog-author": "Tác giả",
    "label-cover-url": "URL ảnh bìa",
    "label-tags-note": "Tags (cách nhau bằng dấu phẩy)",
    "label-summary": "Tóm tắt",
    "label-content": "Nội dung",
    "label-blog-published": "Đã xuất bản",
    "label-blog-draft": "Bản nháp",
    "modal-confirm-delete-title": "️ Xác nhận xóa",
    "label-confirm-delete-msg": "Bạn có chắc muốn xóa?",
    "btn-delete": "Xóa",
    "opt-yes": "Có",
    "opt-no": "Không"
  },
  en: {
    "login-system-title": "Admin Management System",
    "login-email-label": "Admin Email",
    "login-password-label": "Password",
    "login-btn": "Login",
    "login-error-msg": "Incorrect login information!",
    "sidebar-admin-panel": "Admin Panel",
    "nav-dashboard": "Dashboard",
    "nav-products": "Products",
    "nav-orders": "Orders",
    "nav-users": "Users",
    "nav-employees": "Employees",
    "nav-blogs": "Blogs",
    "nav-coupons": "Vouchers",
    "nav-reviews": "Reviews",
    "nav-settings": "Settings",
    "status-connecting": "Connecting...",
    "status-online": "Firebase Connected",
    "status-offline": "Disconnected...",
    "btn-refresh": " Refresh",
    "btn-logout": " Logout",
    "stat-products": "Products",
    "stat-orders": "Orders",
    "stat-users": "Users",
    "stat-blogs": "Blogs",
    "stat-reviews": "Reviews",
    "stat-revenue": "Total Revenue",
    "card-revenue-growth": " Revenue Growth (Last 7 Days)",
    "card-recent-orders": " Recent Orders",
    "card-order-stats": " Order Statistics",
    "label-by-status": "By Status",
    "label-popular-categories": "Popular Categories",
    "th-order-id": "Order ID",
    "th-customer": "Customer",
    "th-items": "Items",
    "th-total": "Total Amount",
    "th-status": "Status",
    "th-email": "Email",
    "th-payment": "Payment",
    "th-date": "Created Date",
    "th-image": "Image",
    "th-id": "ID",
    "th-product-name": "Product Name",
    "th-price": "Price",
    "th-stock": "Stock",
    "th-category": "Category",
    "th-rating": "Rating",
    "th-action": "Action",
    "th-avatar": "Avatar",
    "th-name": "Name",
    "th-dob": "DOB",
    "th-gender": "Gender",
    "th-role": "Role",
    "th-fullname": "Full Name",
    "th-position": "Position",
    "th-shift": "Shift",
    "th-cover": "Cover",
    "th-title": "Title",
    "th-author": "Author",
    "th-tags": "Tags",
    "th-product": "Product",
    "th-user": "User",
    "th-review": "Review",
    "th-content": "Content",
    "settings-title": " System Settings & UI",
    "label-shop-name": "Shop Name",
    "label-language": "Dashboard Language",
    "label-theme": "Display Mode",
    "label-shipping-fee": "Shipping Fee",
    "label-min-freeship": "Min for Free Shipping",
    "label-address": "Office Address",
    "label-phone": "Support Phone",
    "label-contact-email": "Contact Email",
    "btn-save-settings": "Save Settings",
    "placeholder-search-products": " Search products...",
    "placeholder-search-orders": " Search orders...",
    "placeholder-search-users": " Search users...",
    "placeholder-search-employees": " Search employees...",
    "placeholder-search-blogs": " Search blogs...",
    "placeholder-search-coupons": " Search vouchers...",
    "placeholder-search-reviews": " Search reviews...",
    "btn-add-product": "+ Add Product",
    "btn-add-user": "+ Add User",
    "btn-add-employee": "+ Add Employee",
    "btn-add-blog": "+ Add Blog",
    "btn-add-coupon": "+ Add Voucher",
    "opt-all-status": "All Status",
    "opt-pending": "Pending",
    "opt-confirmed": "Confirmed",
    "opt-shipping": "Shipping",
    "opt-delivered": "Delivered",
    "opt-cancelled": "Cancelled",
    "opt-active": "Active",
    "opt-expired": "Expired",
    "opt-scheduled": "Scheduled",
    "opt-all": "All",
    "opt-approved": "Approved",
    "opt-pending-review": "Pending Review",
    "toast-welcome-admin": "Welcome back Admin!",
    "toast-no-permission": "This account does not have Admin access!",
    "toast-incorrect-pass": "Incorrect password!",
    "toast-email-not-found": "Email does not exist in the system!",
    "toast-refreshing": "Refreshing data from Firebase...",
    "toast-error-conn": "Firebase connection error!",
    "confirm-logout": "Are you sure you want to logout?",
    "confirm-delete": "Confirm delete this data?",
    "card-review-topics": " Review Topic Distribution",
    "label-analysis-note": "Based on content keyword analysis (AI Analysis)",
    "modal-product-title": "Add/Edit Product",
    "modal-order-detail-title": "Order Details",
    "modal-add-order-title": "Add New Order",
    "modal-user-title": "User Account",
    "modal-employee-title": "Employee Management",
    "modal-blog-title": "Blog Post",
    "modal-coupon-title": "Voucher Management",
    "th-coupon-code": "Code",
    "th-discount": "Discount",
    "th-expiry": "Expiry",
    "th-usage": "Usage",
    "label-coupon-code": "Voucher Code *",
    "label-coupon-title": "Voucher Title *",
    "label-coupon-discount": "Discount Value (VND or %)",
    "label-coupon-expiry": "Expiry Date (dd/MM/yyyy)",
    "label-product-id": "Product ID *",
    "label-category-id": "Category ID",
    "opt-select-category": "-- Select Category --",
    "label-product-name": "Product Name *",
    "label-original-price": "Original Price (VND)",
    "label-price-vnd": "Sale Price (VND)",
    "label-stock": "Stock",
    "label-active": "Active",
    "label-featured": "Featured",
    "label-img-url": "Image URL (one per line)",
    "label-description": "Description",
    "btn-cancel": "Cancel",
    "btn-save": " Save",
    "btn-close": "Close",
    "btn-update": "Update",
    "label-update-status": "Update Status:",
    "label-customer-name": "Customer *",
    "label-phone-short": "Phone *",
    "label-shipping-address": "Shipping Address *",
    "label-order-items": "Products (Ex: 1x T-shirt (M) | 1x Jean (32)) *",
    "label-total-vnd": "Total (VND) *",
    "label-payment-method": "Payment Method",
    "label-note": "Note",
    "label-dob-format": "DOB (dd/MM/yyyy)",
    "opt-male": "Male",
    "opt-female": "Female",
    "opt-other": "Other",
    "opt-no-reveal": "Prefer not to say",
    "opt-active": "Active",
    "opt-locked": "Locked",
    "label-position": "Position",
    "label-department": "Department",
    "label-blog-title": "Title *",
    "label-blog-author": "Author",
    "label-cover-url": "Cover Image URL",
    "label-tags-note": "Tags (comma separated)",
    "label-summary": "Summary",
    "label-content": "Content",
    "label-blog-published": "Published",
    "label-blog-draft": "Draft",
    "modal-confirm-delete-title": " Confirm Delete",
    "label-confirm-delete-msg": "Are you sure you want to delete?",
    "btn-delete": "Delete",
    "opt-yes": "Yes",
    "opt-no": "No"
  }
};

const currentLang = () => localStorage.getItem('admin-lang') || 'vi';

function getLocalizedProp(obj, propName) {
  if (!obj) return '';
  const lang = currentLang();
  const keys = Object.keys(obj);
  const getValue = (keyName) => {
    if (obj[keyName] !== undefined && obj[keyName] !== null) return obj[keyName];
    const lowerKey = keyName.toLowerCase();
    const foundKey = keys.find(k => k.toLowerCase() === lowerKey);
    if (foundKey && obj[foundKey] !== undefined && obj[foundKey] !== null) return obj[foundKey];
    return undefined;
  };
  if (lang === 'en') {
    const enVal = getValue(`${propName}_en`);
    if (enVal !== undefined && enVal !== '') return enVal;
  } else if (lang === 'zh') {
    const zhVal = getValue(`${propName}_zh`);
    if (zhVal !== undefined && zhVal !== '') return zhVal;
  }
  const defaultVal = getValue(propName);
  return defaultVal !== undefined ? defaultVal : '';
}

function getNextId(list, prefix, startNum = 1) {
  let maxNum = 0;
  if (list && Array.isArray(list)) {
    list.forEach(item => {
      const id = item.id || item.Id || item.ID || '';
      if (id.startsWith(prefix)) {
        const numPart = parseInt(id.substring(prefix.length));
        if (!isNaN(numPart) && numPart > maxNum) {
          maxNum = numPart;
        }
      }
    });
  }
  const nextNum = maxNum > 0 ? maxNum + 1 : startNum;
  if (prefix === 'ORD') {
    return prefix + nextNum;
  } else {
    return prefix + String(nextNum).padStart(3, '0');
  }
}

function t(key) {
  const lang = currentLang();
  return (i18n[lang] && i18n[lang][key]) || key;
}

// Hàm chuẩn hóa tiếng Việt để tìm kiếm không dấu
function normalizeStr(str) {
  if (!str) return '';
  return str.toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[đĐ]/g, 'd')
    .trim();
}

function applyTranslations() {
  const lang = currentLang();
  document.querySelectorAll('[data-i18n]').forEach(el => {
    const key = el.getAttribute('data-i18n');
    if (i18n[lang] && i18n[lang][key]) {
      if (el.tagName === 'INPUT' && el.placeholder) {
        el.placeholder = i18n[lang][key];
      } else {
        el.textContent = i18n[lang][key];
      }
    }
  });
}

const firebaseConfig = {
  apiKey: "AIzaSyADi0v32E1lrSIjinoSk9ozHQLvntqDzhs",
  authDomain: "saive-403f7.firebaseapp.com",
  databaseURL: "https://saive-403f7-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "saive-403f7",
  storageBucket: "saive-403f7.firebasestorage.app",
  messagingSenderId: "403872194717",
  appId: "1:403872194717:android:b05832e79e03ada71eb474"
};

firebase.initializeApp(firebaseConfig);
const db = firebase.database();

// Kiểm tra trạng thái kết nối Firebase
const connectedRef = db.ref(".info/connected");
connectedRef.on("value", (snap) => {
  const dot = $('status-dot');
  const text = $('status-text');
  if (snap.val() === true) {
    dot.className = 'status-dot online';
    text.textContent = t('status-online');
  } else {
    dot.className = 'status-dot offline';
    text.textContent = t('status-offline');
  }
});

const REVIEW_TOPICS = {
  PRODUCT: {
    label: { vi: 'Sản phẩm', en: 'Product' },
    keywords: [
      'chất lượng', 'bền', 'dễ hỏng', 'chắc chắn', 'mỏng manh', 'vải', 'chất vải', 'xịn', 'nát', 'hỏng', 'rách', 'yếu',
      'tính năng', 'chức năng', 'hoạt động', 'đầy đủ', 'thiếu', 'sử dụng', 'tiện lợi',
      'đẹp', 'xấu', 'màu', 'màu sắc', 'kiểu dáng', 'kích thước', 'size', 'mẫu mã', 'thiết kế', 'form', 'dáng',
      'mạnh', 'tiêu thụ điện', 'pin', 'nóng', 'mượt',
      'mô tả', 'vừa', 'vừa vặn', 'rộng', 'chật', 'phù hợp', 'đúng mô tả', 'y hình', 'giống ảnh'
    ],
    color: '#810100' // Saive Maroon
  },
  PRICE: {
    label: { vi: 'Giá cả', en: 'Price' },
    keywords: [
      'giá', 'đắt', 'rẻ', 'tiền', 'chi phí', 'thấp', 'cao', 'tầm giá', 'giá cả', 'hạt dẻ',
      'đáng tiền', 'xứng đáng', 'đáng mua', 'worth', 'tiết kiệm',
      'khuyến mãi', 'giảm giá', 'deal', 'voucher', 'sale', 'quà', 'tặng',
      'cạnh tranh', 'rẻ hơn'
    ],
    color: '#9C927E' // Muted Sand
  },
  SERVICE: {
    label: { vi: 'Dịch vụ', en: 'Service' },
    keywords: [
      'shop', 'người bán', 'chủ shop', 'cửa hàng', 'gian hàng',
      'tư vấn', 'giải đáp', 'hướng dẫn',
      'hỗ trợ', 'giải quyết', 'chăm sóc',
      'thân thiện', 'lịch sự', 'chuyên nghiệp', 'nhiệt tình', 'dễ thương', 'chu đáo',
      'phản hồi', 'trả lời', 'rep', 'tin nhắn'
    ],
    color: '#C19A6B' // Leather / Camel
  },
  DELIVERY: {
    label: { vi: 'Giao hàng', en: 'Delivery' },
    keywords: [
      'giao', 'ship', 'vận chuyển', 'nhanh', 'chậm', 'trễ', 'lâu', 'đúng hẹn', 'tốc độ',
      'shipper', 'giao tận nơi', 'bác tài', 'anh ship', 'người giao',
      'đóng gói', 'hộp', 'bọc', 'gói', 'bao bì', 'chống sốc', 'kỹ', 'cẩn thận',
      'nguyên vẹn', 'móp', 'méo', 'hư hại', 'vỡ', 'bể',
      'đổi trả', 'hoàn tiền',
      'phí ship', 'tiền ship', 'ship mắc', 'free ship', 'miễn phí ship',
      'thiếu hàng', 'đủ số lượng',
      'nhầm', 'sai hàng', 'nhầm hàng', 'đúng mẫu', 'sai mẫu'
    ],
    color: '#5E7182' // Steel Blue Gray
  }
};

const state = {
  products: [],
  orders: [],
  users: [],
  employees: [],
  blogs: [],
  vouchers: [],
  reviews: [],
  settings: {},
  currentPage: 'dashboard'
};

// ── Helpers ────────────────────────────────────────────
function getReviewContent(r) {
  if (!r) return "";
  return (r.Content || r.Comment || r.content || r.comment || "").trim();
}
function fmt(n) {
  const lang = currentLang() === 'en' ? 'en-US' : 'vi-VN';
  return new Intl.NumberFormat(lang, { style: 'currency', currency: 'VND' }).format(n || 0);
}

function fmtDate(ts) {
  if (!ts) return '';
  const lang = currentLang() === 'en' ? 'en-US' : 'vi-VN';
  try {
    let d;
    // 1. Xử lý chuỗi định dạng dd/MM/yyyy hoặc dd-MM-yyyy (Android)
    if (typeof ts === 'string' && (ts.includes('/') || ts.includes('-'))) {
      const parts = ts.split(/[\/\s-]/);
      if (parts.length >= 3) {
        const day = parseInt(parts[0]), month = parseInt(parts[1]), year = parseInt(parts[2]);
        if (year > 1000 && month > 0 && month <= 12 && day > 0 && day <= 31) {
          d = new Date(year, month - 1, day);
        }
      }
    }

    if (!d || isNaN(d.getTime())) d = new Date(ts);
    if (isNaN(d.getTime())) return String(ts);

    return d.toLocaleDateString(lang, { day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch (e) {
    return String(ts || '');
  }
}

function getISODate(ts) {
  if (!ts) return "";
  try {
    let d;
    if (typeof ts === 'string' && (ts.includes('/') || ts.includes('-'))) {
      const parts = ts.split(/[\/\s-]/);
      if (parts.length >= 3) {
        const day = parseInt(parts[0]), month = parseInt(parts[1]), year = parseInt(parts[2]);
        if (year > 1000) d = new Date(year, month - 1, day);
      }
    }
    if (!d || isNaN(d.getTime())) d = new Date(ts);
    if (isNaN(d.getTime())) return "";
    return d.toISOString().split('T')[0];
  } catch (e) { return ""; }
}

window.closeModal = function(id) {
  $(id).style.display = 'none';
}

function showModal(id) {
  $(id).style.display = 'flex';
}

// ── Navigation ─────────────────────────────────────────
window.navigate = function(page) {
  state.currentPage = page;

  // UI Update
  document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
  const navItem = $(`nav-${page}`);
  if (navItem) navItem.classList.add('active');

  document.querySelectorAll('.page').forEach(p => p.style.display = 'none');
  const pageEl = $(`page-${page}`);
  if (pageEl) {
    pageEl.style.display = 'block';
    if (!pageEl.classList.contains('active')) {
       pageEl.classList.add('active');
    }
  }

  $(`page-title`).textContent = t(`nav-${page}`);

  loadData();
}

window.toggleSidebar = function() {
  $('sidebar').classList.toggle('open');
}

// ── Data Loading ───────────────────────────────────────
window.refreshData = function() {
  showToast(t('toast-refreshing'), 'info');
  loadData();
};

async function loadData() {
  try {
    // Lấy dữ liệu Vouchers
    db.ref('Vouchers').on('value', (snapshot) => {
      state.vouchers = Object.entries(snapshot.val() || {}).map(([id, v]) => ({ id, ...v }));
      if (state.currentPage === 'vouchers') renderVouchers();
      if (state.currentPage === 'dashboard') renderStats();
    });

    const [pS, oS, uS, eS, bS, sS, rS] = await Promise.all([
      db.ref('Products').once('value'),
      db.ref('Orders').once('value'),
      db.ref('Users').once('value'),
      db.ref('Employees').once('value'),
      db.ref('Blogs').once('value'),
      db.ref('Settings').once('value'),
      db.ref('Reviews').once('value')
    ]);

    state.products = Object.entries(pS.val() || {}).map(([id, v]) => ({ id, ...v }));
    state.orders = Object.entries(oS.val() || {}).map(([id, v]) => ({ id, ...v }));
    state.users = Object.entries(uS.val() || {}).map(([id, v]) => ({ id, ...v }));
    state.employees = Object.entries(eS.val() || {}).map(([id, v]) => ({ id, ...v }));
    state.blogs = Object.entries(bS.val() || {}).map(([id, v]) => ({ id, ...v }));
    state.settings = sS.val() || {};
    state.reviews = Object.entries(rS.val() || {}).map(([id, v]) => ({ id, ...v }));

    renderStats();
    renderCurrentPage();
  } catch (error) {
    console.error("Load data error:", error);
    showToast(t('toast-error-conn'), 'error');
  }
}

function showToast(msg, type = 'success') {
  const container = $('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = msg;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
}

function renderStats() {
  if (state.currentPage !== 'dashboard') return;

  // Sync logic with Android DashboardViewModel.java:
  // Total revenue only counts 'delivered', 'deliveried', or 'Đã giao'
  const revenue = state.orders.reduce((sum, o) => {
    const s = (o.Status || o.status || '').toLowerCase();
    if (s === 'delivered' || s === 'deliveried' || s === 'đã giao') {
      return sum + (Number(o.TotalAmount || o.totalAmount) || 0);
    }
    return sum;
  }, 0);

  // Sync logic: Total products only counts active items
  const activeProducts = state.products.filter(p => p.IsActive !== false);

  if ($('count-revenue')) $('count-revenue').textContent = fmt(revenue);
  if ($('count-orders')) $('count-orders').textContent = state.orders.length;
  if ($('count-users')) $('count-users').textContent = state.users.length;
  if ($('count-products')) $('count-products').textContent = activeProducts.length;
  if ($('count-blogs')) $('count-blogs').textContent = state.blogs.length;
  if ($('count-vouchers')) $('count-vouchers').textContent = state.vouchers.length;
  if ($('count-reviews')) $('count-reviews').textContent = state.reviews?.length || 0;

  // Render Charts
  renderStatusChart();
  renderCategoryChart();
  renderRevenueGrowthChart();
  renderReviewTopicChart();

  const latest = [...state.orders].sort((a,b) => (b.CreatedAt || 0) - (a.CreatedAt || 0)).slice(0, 6);
  const tbody = $('dashboard-orders-body');
  if (tbody) {
    tbody.innerHTML = latest.map(o => {
      let s = (o.Status || o.status || 'Pending').toLowerCase();
      let key = 'pending';
      if (s.includes('pending') || s.includes('chờ')) key = 'pending';
      else if (s.includes('confirm') || s.includes('xác nhận')) key = 'confirmed';
      else if (s.includes('shipping') || s.includes('đang giao')) key = 'shipping';
      else if (s.includes('delivered') || s.includes('đã giao') || s.includes('complete') || s.includes('hoàn thành')) key = 'delivered';
      else if (s.includes('cancel') || s.includes('hủy')) key = 'cancelled';

      const statusLabel = t('opt-' + key);
      const badgeClass = key === 'delivered' ? 'badge-success' : (key === 'cancelled' ? 'badge-danger' : 'badge-warning');

      return `
        <tr>
          <td>#${(o.id || '').substring(0,8).toUpperCase()}</td>
          <td>${o.FullName || o.fullName || 'Guest'}</td>
          <td style="font-size: 0.8rem; color: var(--text-secondary);">${o.ItemsSummary || (o.Items ? o.Items.length + ' sp' : '—')}</td>
          <td style="font-weight:700;">${fmt(o.TotalAmount || o.totalAmount)}</td>
          <td><span class="badge ${badgeClass}">${statusLabel}</span></td>
        </tr>
      `;
    }).join('');
  }
}

function renderStatusChart() {
  const container = $('order-status-chart');
  if (!container) return;

  const stats = state.orders.reduce((acc, o) => {
    let s = (o.Status || o.status || 'Pending').toLowerCase();
    // Normalize to internal keys
    let key = 'pending';
    if (s.includes('pending') || s.includes('chờ')) key = 'pending';
    else if (s.includes('confirm') || s.includes('xác nhận')) key = 'confirmed';
    else if (s.includes('shipping') || s.includes('đang giao')) key = 'shipping';
    else if (s.includes('delivered') || s.includes('đã giao') || s.includes('complete') || s.includes('hoàn thành')) key = 'delivered';
    else if (s.includes('cancel') || s.includes('hủy')) key = 'cancelled';

    acc[key] = (acc[key] || 0) + 1;
    return acc;
  }, {});

  const total = state.orders.length || 1;
  const colors = {
    'pending': 'var(--warning)',
    'confirmed': 'var(--info)',
    'shipping': 'var(--maroon-light)',
    'delivered': 'var(--success)',
    'cancelled': 'var(--danger)'
  };

  container.innerHTML = Object.entries(stats).map(([key, count]) => {
    const pct = (count / total * 100).toFixed(0);
    const color = colors[key] || '#64748b';
    const label = t('opt-' + key); // Use translation function for unified language
    return `
      <div class="status-bar-item">
        <div class="status-bar-label">
          <span class="status-bar-name">${label}</span>
          <span class="status-bar-count">${count} (${pct}%)</span>
        </div>
        <div class="status-bar-track">
          <div class="status-bar-fill" style="width: ${pct}%; background: ${color}"></div>
        </div>
      </div>
    `;
  }).join('');
}

function renderCategoryChart() {
  const container = $('category-chart');
  if (!container) return;

  const catStats = {};
  state.orders.forEach(o => {
    (o.Items || []).forEach(item => {
      let cat = item.Category || item.CategoryId || item.category;
      if (!cat && item.ProductId) {
        const p = state.products.find(x => x.id === item.ProductId || x.ProductId === item.ProductId);
        if (p) cat = p.CategoryId || p.category;
      }
      cat = cat || 'General';
      catStats[cat] = (catStats[cat] || 0) + (Number(item.Quantity) || 1);
    });
  });

  const total = Object.values(catStats).reduce((a, b) => a + b, 0) || 1;

  container.innerHTML = Object.entries(catStats)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map(([label, count]) => {
      const pct = (count / total * 100).toFixed(0);
      return `
        <div class="status-bar-item">
          <div class="status-bar-label">
            <span class="status-bar-name">${label}</span>
            <span class="status-bar-count">${count} sp (${pct}%)</span>
          </div>
          <div class="status-bar-track">
            <div class="status-bar-fill" style="width: ${pct}%; background: var(--maroon)"></div>
          </div>
        </div>
      `;
    }).join('');
}

function renderRevenueGrowthChart() {
  const container = $('revenue-growth-chart');
  if (!container) return;

  const last7Days = [];
  const lang = currentLang() === 'en' ? 'en-US' : 'vi-VN';
  for (let i = 6; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    d.setHours(0,0,0,0);
    last7Days.push({
      timestamp: d.getTime(),
      label: d.toLocaleDateString(lang, { day: '2-digit', month: '2-digit' }),
      revenue: 0
    });
  }

  state.orders.forEach(o => {
    const ts = o.CreatedAt || o.timestamp;
    if (!ts) return;
    const orderDate = new Date(ts);
    orderDate.setHours(0,0,0,0);
    const orderTs = orderDate.getTime();

    const dayEntry = last7Days.find(d => d.timestamp === orderTs);
    if (dayEntry) {
      dayEntry.revenue += (Number(o.TotalAmount) || 0);
    }
  });

  const maxRevenue = Math.max(...last7Days.map(d => d.revenue), 1000000);

  container.innerHTML = last7Days.map(d => {
    const pct = (d.revenue / maxRevenue * 100);
    return `
      <div class="revenue-bar-container">
        <div class="revenue-bar" style="height: ${pct}%" data-value="${fmt(d.revenue)}"></div>
        <div class="revenue-date">${d.label}</div>
      </div>
    `;
  }).join('');
}

function renderReviewTopicChart() {
  const container = $('review-topic-chart');
  if (!container || !state.reviews || state.reviews.length === 0) {
    if (container) container.innerHTML = '<div style="text-align:center; padding:20px; color:var(--text-muted);">Chưa có dữ liệu đánh giá</div>';
    return;
  }

  const stats = { PRODUCT: 0, DELIVERY: 0, PRICE: 0, SERVICE: 0, OTHER: 0 };
  const lang = currentLang();

  state.reviews.forEach(r => {
    const text = getReviewContent(r).toLowerCase();
    if (!text) return;

    let matchedAny = false;

    // PRODUCT logic with exclusions (matching Android DashboardViewModel.java)
    let hasProd = false;
    if (text.match(/chất lượng|bền|dễ hỏng|chắc chắn|mỏng manh|vải|chất vải|xịn|nát|hỏng|rách|yếu/)) hasProd = true;
    if (text.match(/tính năng|chức năng|hoạt động|đầy đủ|thiếu|sử dụng|tiện lợi/)) hasProd = true;
    if (text.match(/đẹp|xấu|màu|màu sắc|kiểu dáng|kích thước|size|mẫu mã|thiết kế|form|dáng/)) hasProd = true;
    if (text.match(/nhanh|chậm|mạnh|yếu|tiêu thụ điện|pin|nóng|mượt/) && !text.match(/giao|ship|vận chuyển/)) hasProd = true;
    if (text.match(/mô tả|vừa|vừa vặn|rộng|chật|phù hợp|đúng mô tả|y hình|giống ảnh/)) hasProd = true;
    if (hasProd) { stats.PRODUCT++; matchedAny = true; }

    // PRICE logic
    if (text.match(/giá|đắt|rẻ|tiền|chi phí|thấp|cao|tầm giá|giá cả|hạt dẻ|đáng tiền|xứng đáng|đáng mua|worth|tiết kiệm|khuyến mãi|giảm giá|deal|voucher|sale|quà|tặng|cạnh tranh|rẻ hơn/)) {
      stats.PRICE++; matchedAny = true;
    }

    // SERVICE logic
    if (text.match(/shop|người bán|chủ shop|cửa hàng|gian hàng|tư vấn|giải đáp|hướng dẫn|hỗ trợ|giải quyết|chăm sóc|thân thiện|lịch sự|chuyên nghiệp|nhiệt tình|dễ thương|chu đáo|phản hồi|trả lời|rep|tin nhắn/)) {
      stats.SERVICE++; matchedAny = true;
    }

    // DELIVERY logic
    if (text.match(/giao|ship|vận chuyển|nhanh|chậm|trễ|lâu|đúng hẹn|tốc độ|shipper|giao tận nơi|bác tài|anh ship|người giao|đóng gói|hộp|bọc|gói|bao bì|chống sốc|kỹ|cẩn thận|nguyên vẹn|móp|méo|hư hại|vỡ|bể|đổi trả|hoàn tiền|phí ship|tiền ship|ship mắc|free ship|miễn phí ship|thiếu hàng|đủ số lượng|nhầm|sai hàng|nhầm hàng|đúng mẫu|sai mẫu/)) {
      stats.DELIVERY++; matchedAny = true;
    }

    if (!matchedAny) stats.OTHER++;
  });

  const total = state.reviews.length || 1;
  let html = '';
  const sortedTopics = Object.keys(REVIEW_TOPICS).sort((a, b) => stats[b] - stats[a]);

  for (const topic of sortedTopics) {
    const config = REVIEW_TOPICS[topic];
    const count = stats[topic];
    const pct = ((count / total) * 100).toFixed(0);
    const label = config.label[lang] || config.label['vi'];

    html += `
      <div class="status-bar-item">
        <div class="status-bar-label">
          <span class="status-bar-name">${label}</span>
          <span class="status-bar-count">${count} (${pct}%)</span>
        </div>
        <div class="status-bar-track">
          <div class="status-bar-fill" style="width: ${pct}%; background: ${config.color}"></div>
        </div>
      </div>
    `;
  }

  if (stats.OTHER > 0) {
    const otherPct = ((stats.OTHER / total) * 100).toFixed(0);
    html += `
      <div class="status-bar-item">
        <div class="status-bar-label">
          <span class="status-bar-name">${lang === 'en' ? 'Other' : 'Khác'}</span>
          <span class="status-bar-count">${stats.OTHER} (${otherPct}%)</span>
        </div>
        <div class="status-bar-track">
          <div class="status-bar-fill" style="width: ${otherPct}%; background: #94a3b8"></div>
        </div>
      </div>
    `;
  }
  container.innerHTML = html;
}


// ── Charts & Analytics ──────────────────────────────
let wordCloudChart = null;
let treemapChart = null;

window.showReviewAnalytics = function() {
  showModal('review-analytics-modal');
  setTimeout(initAnalyticsCharts, 300);
}

function initAnalyticsCharts() {
  const wcEl = $('analytics-wordcloud');
  const tmEl = $('analytics-treemap');

  // Dispose existing instances to avoid memory leaks and ensure theme updates apply
  if (wordCloudChart) wordCloudChart.dispose();
  if (treemapChart) treemapChart.dispose();

  wordCloudChart = echarts.init(wcEl);
  treemapChart = echarts.init(tmEl);

  const data = computeReviewAnalytics();
  renderWordCloud(data.wordCloud);
  renderTreemap(data.treemap);

  // CLICK LISTENERS: Filter reviews by clicking keywords/topics (User Requirement)
  wordCloudChart.on('click', (params) => {
    closeModal('review-analytics-modal');
    navigate('reviews');
    setTimeout(() => {
      if ($('review-search')) {
        $('review-search').value = params.name;
        filterReviews();
      }
    }, 400);
  });

  treemapChart.on('click', (params) => {
    let query = params.name;
    // Extract Vietnamese keyword if present in brackets "Quality (Chất lượng)" -> "Chất lượng"
    if (query.includes('(')) {
      const parts = query.match(/\((.*?)\)/);
      if (parts && parts[1]) query = parts[1];
    }
    closeModal('review-analytics-modal');
    navigate('reviews');
    setTimeout(() => {
      if ($('review-search')) {
        $('review-search').value = query;
        filterReviews();
      }
    }, 400);
  });

  // Use a named function for the resize listener to prevent multiple bindings
  if (!window._resizeHandlerBound) {
    window.addEventListener('resize', () => {
      wordCloudChart?.resize();
      treemapChart?.resize();
    });
    window._resizeHandlerBound = true;
  }
}

window.switchAnalyticsTab = function(tab) {
  const wc = $('analytics-wordcloud');
  const tm = $('analytics-treemap');
  const btnWc = $('btn-tab-wc');
  const btnTm = $('btn-tab-tm');

  if (tab === 'wordcloud') {
    wc.style.display = 'block';
    tm.style.display = 'none';
    btnWc.classList.add('active');
    btnTm.classList.remove('active');
    setTimeout(() => wordCloudChart?.resize(), 50);
  } else {
    wc.style.display = 'none';
    tm.style.display = 'block';
    btnWc.classList.remove('active');
    btnTm.classList.add('active');
    // Important: ECharts needs resize after container becomes visible
    setTimeout(() => treemapChart?.resize(), 50);
  }
}

function computeReviewAnalytics() {
  const stopWords = new Set([
    "và", "có", "là", "thì", "mà", "của", "được", "cho", "trong", "ra", "lại", "ở", "bị", "cái", "này", "như", "nó", "đây", "đó",
    "với", "các", "những", "một", "nhưng", "cũng", "đã", "đang", "sẽ", "đi", "về", "lên", "xuống", "vào", "đến",
    "nhiều", "ít", "quá", "rất", "khá", "để", "nếu", "khi", "vì", "nên", "tự", "mình", "họ", "ta", "tôi", "bạn",
    "nha", "ạ", "nhé", "nhe", "ấy", "đều", "hơn", "nhất", "không", "ko", "k", "khg", "chưa", "được", "rồi", "nữa",
    "cực", "kỳ", "thật", "luôn", "thấy", "lại", "còn", "chỉ", "sự", "việc", "sản", "phẩm", "sp", "hàng", "cửa", "hàng"
  ]);

  const wordFreq = {};
  const subStats = {
    PRODUCT: { quality: 0, features: 0, design: 0, performance: 0, fit: 0 },
    PRICE: { cost: 0, value: 0, promo: 0, compare: 0 },
    SERVICE: { shop: 0, consult: 0, support: 0, attitude: 0, response: 0 },
    DELIVERY: { speed: 0, shipper: 0, pkg: 0, status: 0, policy: 0, fee: 0, qty: 0, target: 0 }
  };

  state.reviews.forEach(r => {
    const text = getReviewContent(r).toLowerCase();
    if (!text) return;

    // Word Cloud Logic
    const cleanText = text.replace(/[^\w\sàáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệđìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ]/g, " ");
    const words = cleanText.split(/\s+/);
    words.forEach(w => {
      if (w.length > 2 && !stopWords.has(w) && !/^\d+$/.test(w)) {
        wordFreq[w] = (wordFreq[w] || 0) + 1;
      }
    });

    // Treemap / Topic Logic (Sync with Android logic)
    if (text.match(/chất lượng|bền|dễ hỏng|chắc chắn|mỏng manh|vải|chất vải|xịn|nát|hỏng|rách|yếu/)) subStats.PRODUCT.quality++;
    if (text.match(/tính năng|chức năng|hoạt động|đầy đủ|thiếu|sử dụng|tiện lợi/)) subStats.PRODUCT.features++;
    if (text.match(/đẹp|xấu|màu|màu sắc|kiểu dáng|kích thước|size|mẫu mã|thiết kế|form|dáng/)) subStats.PRODUCT.design++;
    if (text.match(/nhanh|chậm|mạnh|yếu|tiêu thụ điện|pin|nóng|mượt/) && !text.match(/giao|ship|vận chuyển/)) subStats.PRODUCT.performance++;
    if (text.match(/mô tả|vừa|vừa vặn|rộng|chật|phù hợp|đúng mô tả|y hình|giống ảnh/)) subStats.PRODUCT.fit++;

    if (text.match(/giá|đắt|rẻ|tiền|chi phí|thấp|cao|tầm giá|giá cả|hạt dẻ/)) subStats.PRICE.cost++;
    if (text.match(/đáng tiền|xứng đáng|đáng mua|worth|tiết kiệm/)) subStats.PRICE.value++;
    if (text.match(/khuyến mãi|giảm giá|deal|voucher|sale|quà|tặng/)) subStats.PRICE.promo++;
    if (text.match(/cạnh tranh|rẻ hơn/)) subStats.PRICE.compare++;

    if (text.match(/shop|người bán|chủ shop|cửa hàng|gian hàng/)) subStats.SERVICE.shop++;
    if (text.match(/tư vấn|giải đáp|hướng dẫn/)) subStats.SERVICE.consult++;
    if (text.match(/hỗ trợ|giải quyết|chăm sóc/)) subStats.SERVICE.support++;
    if (text.match(/thân thiện|lịch sự|chuyên nghiệp|nhiệt tình|dễ thương|chu đáo/)) subStats.SERVICE.attitude++;
    if (text.match(/phản hồi|trả lời|nhanh|rep|tin nhắn/)) subStats.SERVICE.response++;

    if (text.match(/giao|ship|vận chuyển|nhanh|chậm|trễ|lâu|đúng hẹn|tốc độ/) && text.match(/giao|ship|vận chuyển/)) subStats.DELIVERY.speed++;
    if (text.match(/shipper|giao tận nơi|bác tài|anh ship|người giao/)) subStats.DELIVERY.shipper++;
    if (text.match(/đóng gói|hộp|bọc|gói|bao bì|chống sốc|kỹ|cẩn thận/)) subStats.DELIVERY.pkg++;
    if (text.match(/nguyên vẹn|móp|méo|hư hại|nát|vỡ|bể/)) subStats.DELIVERY.status++;
    if (text.match(/đổi trả|hoàn tiền/)) subStats.DELIVERY.policy++;
    if (text.match(/phí ship|tiền ship|ship mắc|free ship|miễn phí ship/)) subStats.DELIVERY.fee++;
    if (text.match(/thiếu|đủ|thiếu hàng|đủ số lượng/)) subStats.DELIVERY.qty++;
    if (text.match(/đúng|sai|nhầm|sai hàng|nhầm hàng|đúng mẫu|sai mẫu/)) subStats.DELIVERY.target++;
  });

  const wordCloudData = Object.entries(wordFreq)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 50)
    .map(([name, value]) => ({ name, value }));

  const treemapData = [
    {
      name: 'PRODUCT',
      itemStyle: { color: REVIEW_TOPICS.PRODUCT.color },
      value: Object.values(subStats.PRODUCT).reduce((a, b) => a + b, 0),
      children: [
        { name: 'Quality (Chất lượng)', value: subStats.PRODUCT.quality },
        { name: 'Features (Tính năng)', value: subStats.PRODUCT.features },
        { name: 'Design (Thiết kế)', value: subStats.PRODUCT.design },
        { name: 'Performance (Hiệu năng)', value: subStats.PRODUCT.performance },
        { name: 'Fit (Phù hợp)', value: subStats.PRODUCT.fit }
      ]
    },
    {
      name: 'PRICE',
      itemStyle: { color: REVIEW_TOPICS.PRICE.color },
      value: Object.values(subStats.PRICE).reduce((a, b) => a + b, 0),
      children: [
        { name: 'Cost (Giá cả)', value: subStats.PRICE.cost },
        { name: 'Value (Giá trị)', value: subStats.PRICE.value },
        { name: 'Promos (Khuyến mãi)', value: subStats.PRICE.promo },
        { name: 'Compare (So sánh)', value: subStats.PRICE.compare }
      ]
    },
    {
      name: 'SERVICE',
      itemStyle: { color: REVIEW_TOPICS.SERVICE.color },
      value: Object.values(subStats.SERVICE).reduce((a, b) => a + b, 0),
      children: [
        { name: 'Shop (Cửa hàng)', value: subStats.SERVICE.shop },
        { name: 'Consult (Tư vấn)', value: subStats.SERVICE.consult },
        { name: 'Support (Hỗ trợ)', value: subStats.SERVICE.support },
        { name: 'Attitude (Thái độ)', value: subStats.SERVICE.attitude },
        { name: 'Response (Phản hồi)', value: subStats.SERVICE.response }
      ]
    },
    {
      name: 'DELIVERY',
      itemStyle: { color: REVIEW_TOPICS.DELIVERY.color },
      value: Object.values(subStats.DELIVERY).reduce((a, b) => a + b, 0),
      children: [
        { name: 'Speed (Tốc độ)', value: subStats.DELIVERY.speed },
        { name: 'Shipper', value: subStats.DELIVERY.shipper },
        { name: 'Packaging (Đóng gói)', value: subStats.DELIVERY.pkg },
        { name: 'Status (Tình trạng)', value: subStats.DELIVERY.status },
        { name: 'Policy (Chính sách)', value: subStats.DELIVERY.policy },
        { name: 'Fee (Phí ship)', value: subStats.DELIVERY.fee },
        { name: 'Quantity (Số lượng)', value: subStats.DELIVERY.qty },
        { name: 'Target (Đối tượng)', value: subStats.DELIVERY.target }
      ]
    }
  ];

  return { wordCloud: wordCloudData, treemap: treemapData };
}

function renderWordCloud(data) {
  const isDark = document.body.getAttribute('data-theme') === 'dark';
  const textColor = isDark ? '#ffffff' : '#1B1717';

  wordCloudChart.setOption({
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      sizeRange: [12, 60],
      rotationRange: [-45, 45],
      gridSize: 8,
      textStyle: {
        fontFamily: 'Inter, sans-serif',
        fontWeight: 'bold',
        color: () => {
          const colors = ['#810100', '#5E7182', '#9C927E', '#C19A6B', '#1B1717'];
          if (isDark) colors[4] = '#FAF8F3';
          return colors[Math.floor(Math.random() * colors.length)];
        }
      },
      emphasis: {
        textStyle: {
          shadowBlur: 10,
          shadowColor: '#333'
        }
      },
      data: data
    }]
  });
}

function renderTreemap(data) {
  const isDark = document.body.getAttribute('data-theme') === 'dark';
  const textColor = isDark ? '#ffffff' : '#1B1717';

  treemapChart.setOption({
    tooltip: { formatter: '{b}: {c} mentions' },
    series: [{
      type: 'treemap',
      data: data,
      breadcrumb: { show: false },
      roam: false,
      nodeClick: false,
      levels: [
        {
          itemStyle: {
            gapWidth: 4,
            borderColor: isDark ? '#241f1f' : '#D4C9B8',
            borderWidth: 2
          },
          upperLabel: {
            show: true,
            height: 30,
            color: textColor,
            fontFamily: 'Inter',
            fontWeight: 'bold',
            fontSize: 12
          }
        },
        {
          itemStyle: {
            gapWidth: 1,
            borderColor: 'transparent'
          },
          label: {
            show: true,
            color: '#ffffff',
            fontFamily: 'Inter',
            fontSize: 10,
            formatter: '{b}\n{c}'
          }
        }
      ]
    }]
  });
}

function renderCurrentPage() {
  switch(state.currentPage) {
    case 'dashboard': renderStats(); break;
    case 'products': renderProducts(); break;
    case 'orders': renderOrders(); break;
    case 'users': renderUsers(); break;
    case 'employees': renderEmployees(); break;
    case 'blogs': renderBlogs(); break;
    case 'vouchers': renderVouchers(); break;
    case 'reviews': renderReviews(); break;
    case 'settings': renderSettings(); break;
  }
}

// ── Products ───────────────────────────────────────────
const NO_IMAGE_SVG = `data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='150' height='150' viewBox='0 0 150 150'%3E%3Crect fill='%232d3436' width='150' height='150'/%3E%3Ctext fill='%23636e72' font-family='sans-serif' font-size='14' dy='.3em' x='50%25' y='50%25' text-anchor='middle'%3ENo Image%3C/text%3E%3C/svg%3E`;

function getProductImage(p) {
  if (!p) return NO_IMAGE_SVG;

  // Ưu tiên các trường PascalCase từ Mobile App, sau đó đến camelCase
  const url = p.ImageUrl || p.imageUrl || p.Images || p.images || p.image || p.img || p.Thumbnail || p.thumb || '';

  // Nếu là mảng, lấy phần tử đầu tiên
  if (Array.isArray(url)) {
    return (url.length > 0 && typeof url[0] === 'string' && url[0].startsWith('http')) ? url[0] : NO_IMAGE_SVG;
  }

  // Nếu là string, kiểm tra xem có phải URL hợp lệ không
  if (typeof url === 'string' && url.startsWith('http')) {
    return url;
  }

  // Trường hợp đặc biệt: kiểm tra trong mảng imageUrls hoặc images (nếu url trên chưa tìm thấy)
  const list = p.ImageUrls || p.imageUrls || p.images || [];
  if (Array.isArray(list) && list.length > 0 && typeof list[0] === 'string' && list[0].startsWith('http')) {
    return list[0];
  }

  return NO_IMAGE_SVG;
}

function getProductRating(productId) {
  if (!state.reviews) return '☆☆☆☆☆';
  const productReviews = state.reviews.filter(r => r.ProductId === productId || r.productId === productId);
  if (productReviews.length === 0) return '☆☆☆☆☆';
  const avg = productReviews.reduce((sum, r) => sum + (r.Rating || 0), 0) / productReviews.length;
  const full = Math.round(avg);
  return '<span class="stars">' + '★'.repeat(full) + '☆'.repeat(5 - full) + '</span>';
}

function renderProducts() {
  const tbody = $('products-table-body');
  if (!tbody) return;

  tbody.innerHTML = state.products.map(p => `
    <tr>
      <td><img src="${getProductImage(p)}" class="thumb" onerror="this.src='${NO_IMAGE_SVG}'"></td>
      <td style="font-size:0.75rem; color:var(--text-muted)">${(p.id || '').substring(0,8).toUpperCase()}</td>
      <td style="font-weight:600;">${getLocalizedProp(p, 'ProductName') || 'N/A'}</td>
      <td style="color:var(--accent); font-weight:700;">${fmt(p.Price || p.price)}</td>
      <td>${p.StockQuantity || p.stock || 0}</td>
      <td><span class="badge badge-info">${p.CategoryId || p.category || 'Luxury'}</span></td>
      <td>${getProductRating(p.id)}</td>
      <td><span class="badge ${p.IsActive !== false ? 'badge-success' : 'badge-danger'}">${p.IsActive !== false ? 'Kích hoạt' : 'Ẩn'}</span></td>
      <td style="text-align:right;">
        <div class="action-btns">
          <button class="btn-icon btn-icon-edit" onclick="editProduct('${p.id}')" title="Sửa">✏️</button>
          <button class="btn-icon btn-icon-delete" onclick="deleteNode('Products','${p.id}')" title="Xóa">🗑️</button>
        </div>
      </td>
    </tr>
  `).join('');
}

let currentProductVariants = {};

window.openProductModal = function() {
  $('product-id-field').value = '';
  $('product-ProductId').value = 'P' + Date.now().toString().slice(-4);
  $('product-ProductName').value = '';
  $('product-Price').value = '';
  $('product-CategoryId').value = '';
  $('product-Images').value = '';
  $('product-StockQuantity').value = '0';
  $('product-Description').value = '';
  currentProductVariants = {};
  renderProductVariants();
  showModal('product-modal');
}

function normalizeVariants(p) {
  if (p.Variants) return p.Variants;

  // Hỗ trợ định dạng Stock lồng nhau từ Firebase (XS: { black: 100, ... })
  const stockData = p.Stock || p.stock;
  if (stockData && typeof stockData === 'object' && !Array.isArray(stockData)) {
    const variants = {};
    for (const [size, colors] of Object.entries(stockData)) {
      if (colors && typeof colors === 'object') {
        for (const [color, qty] of Object.entries(colors)) {
          if (typeof qty === 'number' || !isNaN(qty)) {
            const key = `${size}_${color}`;
            variants[key] = {
              Size: size,
              Color: color,
              Stock: Number(qty)
            };
          }
        }
      }
    }
    if (Object.keys(variants).length > 0) return variants;
  }
  return {};
}

window.editProduct = function(id) {
  const p = state.products.find(x => x.id === id);
  if (!p) return;
  $('product-id-field').value = p.id;
  $('product-ProductId').value = p.ProductId || '';
  $('product-ProductName').value = getLocalizedProp(p, 'ProductName');
  $('product-Price').value = p.Price || p.price || '';
  $('product-CategoryId').value = p.CategoryId || p.category || '';
  $('product-Images').value = p.imageUrl || p.imageUrls?.[0] || '';
  $('product-StockQuantity').value = p.StockQuantity || 0;
  $('product-Description').value = getLocalizedProp(p, 'Description');

  currentProductVariants = normalizeVariants(p);
  renderProductVariants();
  showModal('product-modal');
}

window.addProductVariant = function() {
  const size = $('new-variant-size').value.trim();
  const color = $('new-variant-color').value.trim();
  const stock = parseInt($('new-variant-stock').value) || 0;

  if (!size || !color) {
    showToast('Vui lòng nhập đầy đủ Size và Màu sắc!', 'warning');
    return;
  }

  const key = `${size}_${color}`;
  currentProductVariants[key] = {
    Size: size,
    Color: color,
    Stock: stock
  };

  renderProductVariants();

  // Reset
  $('new-variant-size').value = '';
  $('new-variant-color').value = '';
  $('new-variant-stock').value = '0';
}

window.removeProductVariant = function(key) {
  delete currentProductVariants[key];
  renderProductVariants();
}

function renderProductVariants() {
  const tbody = $('product-variants-list');
  if (!tbody) return;

  const entries = Object.entries(currentProductVariants);
  if (entries.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; padding: 15px; color: var(--text-muted);">Chưa có phân loại nào</td></tr>';
    $('product-StockQuantity').value = 0;
    return;
  }

  let totalStock = 0;
  tbody.innerHTML = entries.map(([key, v]) => {
    totalStock += (v.Stock || 0);
    return `
      <tr>
        <td><input type="text" value="${v.Size}" onchange="updateVariantField('${key}', 'Size', this.value)" style="width: 100%; border:none; background:transparent; color:inherit; font-family:inherit;"></td>
        <td><input type="text" value="${v.Color}" onchange="updateVariantField('${key}', 'Color', this.value)" style="width: 100%; border:none; background:transparent; color:inherit; font-family:inherit;"></td>
        <td style="text-align:center;"><input type="number" value="${v.Stock}" onchange="updateVariantField('${key}', 'Stock', this.value)" style="width: 60px; border:1px solid var(--border); border-radius:4px; background:var(--bg-input); color:var(--text-main); text-align:center;"></td>
        <td style="text-align:center;">
          <button type="button" class="btn-icon" onclick="removeProductVariant('${key}')">✕</button>
        </td>
      </tr>
    `;
  }).join('');

  $('product-StockQuantity').value = totalStock;
}

window.updateVariantField = function(key, field, value) {
  if (field === 'Stock') {
    currentProductVariants[key][field] = parseInt(value) || 0;
  } else {
    currentProductVariants[key][field] = value;
  }

  // Nếu đổi Size hoặc Color, chúng ta cần đổi key
  if (field === 'Size' || field === 'Color') {
    const v = currentProductVariants[key];
    const newKey = `${v.Size}_${v.Color}`;
    if (newKey !== key) {
      currentProductVariants[newKey] = v;
      delete currentProductVariants[key];
      // Re-render để cập nhật onclick cho các nút xóa và onchange cho inputs
      renderProductVariants();
      return;
    }
  }

  // Tính lại tổng tồn kho
  let totalStock = 0;
  Object.values(currentProductVariants).forEach(v => {
    totalStock += (v.Stock || 0);
  });
  $('product-StockQuantity').value = totalStock;
}

window.saveProduct = async function() {
  const id = $('product-id-field').value;
  const data = {
    ProductId: $('product-ProductId').value,
    ProductName: $('product-ProductName').value,
    name: $('product-ProductName').value, // compatibility
    Price: Number($('product-Price').value),
    price: Number($('product-Price').value), // compatibility
    CategoryId: $('product-CategoryId').value,
    category: $('product-CategoryId').value, // compatibility
    imageUrl: $('product-Images').value,
    imageUrls: [$('product-Images').value],
    Description: $('product-Description').value,
    description: $('product-Description').value, // compatibility
    StockQuantity: Number($('product-StockQuantity').value),
    Variants: currentProductVariants,
    // Đồng bộ ngược lại cấu trúc Stock cũ (XS/black: qty)
    Stock: (() => {
      const stock = {};
      Object.values(currentProductVariants).forEach(v => {
        if (!stock[v.Size]) stock[v.Size] = {};
        stock[v.Size][v.Color] = v.Stock;
      });
      return stock;
    })(),
    IsActive: $('product-IsActive').value === 'true',
    IsFeatured: $('product-IsFeatured').value === 'true',
    UpdatedAt: Date.now()
  };
  if (!id) data.CreatedAt = Date.now();

  try {
    if (id) {
      await db.ref(`Products/${id}`).update(data);
    } else {
      const nextId = getNextId(state.products, 'PRD', 1);
      data.ProductId = nextId; // keep sync with the DB key
      await db.ref(`Products/${nextId}`).set(data);
    }
    closeModal('product-modal');
    loadData();
  } catch (e) { alert(e.message); }
}

// ── Orders ─────────────────────────────────────────────
function renderOrders() {
  const tbody = $('orders-table-body');
  if (!tbody) return;

  tbody.innerHTML = state.orders.map(o => {
    let s = (o.Status || o.status || 'Pending').toLowerCase();
    let key = 'pending';
    if (s.includes('pending') || s.includes('chờ')) key = 'pending';
    else if (s.includes('confirm') || s.includes('xác nhận')) key = 'confirmed';
    else if (s.includes('shipping') || s.includes('đang giao')) key = 'shipping';
    else if (s.includes('delivered') || s.includes('đã giao') || s.includes('complete') || s.includes('hoàn thành')) key = 'delivered';
    else if (s.includes('cancel') || s.includes('hủy')) key = 'cancelled';

    const statusLabel = t('opt-' + key);
    const badgeClass = key === 'delivered' ? 'badge-success' : (key === 'cancelled' ? 'badge-danger' : 'badge-warning');
    const isoDate = getISODate(o.CreatedAt || o.createdAt || o.timestamp);

    return `
      <tr data-status="${key}" data-date="${isoDate}">
        <td>#${(o.id || '').substring(0,8).toUpperCase()}</td>
        <td>${o.FullName || o.fullName || 'Guest'}</td>
        <td style="font-size:0.8rem;">${o.Email || o.email || '—'}</td>
        <td style="font-weight:700;">${fmt(o.TotalAmount || o.totalAmount)}</td>
        <td><span class="badge badge-muted">${o.PaymentMethod || o.paymentMethod || 'COD'}</span></td>
        <td><span class="badge ${badgeClass}">${statusLabel}</span></td>
        <td style="font-size:0.8rem; color:var(--text-muted)">${fmtDate(o.CreatedAt || o.createdAt || o.timestamp)}</td>
        <td style="text-align:right;">
          <button class="btn-icon btn-icon-view" onclick="viewOrder('${o.id}')" title="Xem chi tiết">👁️</button>
          <button class="btn-icon btn-icon-delete" onclick="deleteNode('Orders','${o.id}')" title="Xóa">🗑️</button>
        </td>
      </tr>
    `;
  }).join('');
  if (window.filterOrders) window.filterOrders();
}

window.viewOrder = function(id) {
  const o = state.orders.find(x => x.id === id);
  if (!o) return;

  let itemsHtml = (o.Items || []).map(item => {
    const prod = state.products.find(x => x.id === item.ProductId || x.ProductId === item.ProductId || x.id === item.productId || x.ProductId === item.productId);
    const pName = prod ? getLocalizedProp(prod, 'ProductName') : (item.ProductName || item.name || 'N/A');
    return `
    <tr>
      <td>
        <div style="font-weight:600;">${pName}</div>
        <div style="font-size:0.7rem; color:var(--text-muted)">ID: ${item.ProductId || '—'}</div>
      </td>
      <td style="text-align:center;">
        <span class="badge badge-muted">${item.SelectedSize || item.selectedSize || item.size || '—'}</span>
        <span style="opacity:0.3; margin:0 4px;">|</span>
        <span class="badge badge-muted">${item.SelectedColor || item.selectedColor || item.color || '—'}</span>
      </td>
      <td style="text-align:center; font-weight:600;">${item.Quantity || 1}</td>
      <td style="text-align:right; font-weight:700; color:var(--maroon);">${fmt(item.Price || item.price)}</td>
    </tr>
  `}).join('');

  if (!itemsHtml && o.ItemsSummary) {
    itemsHtml = `<tr><td colspan="4" style="padding:20px; text-align:center; font-style:italic; color:var(--text-secondary);">${o.ItemsSummary}</td></tr>`;
  }

  $('order-detail-body').innerHTML = `
    <div class="order-detail-grid">
      <div class="order-detail-item"><span class="label">Mã đơn:</span><span class="value">#${o.id.toUpperCase()}</span></div>
      <div class="order-detail-item"><span class="label">Ngày đặt:</span><span class="value">${fmtDate(o.CreatedAt || o.timestamp)}</span></div>
      <div class="order-detail-item"><span class="label">Khách hàng:</span><span class="value">${o.FullName || o.fullName}</span></div>
      <div class="order-detail-item"><span class="label">Số điện thoại:</span><span class="value">${o.Phone || o.phone || '—'}</span></div>
      <div class="order-detail-item full-width"><span class="label">Địa chỉ:</span><span class="value">${o.ShippingAddress || o.address || 'N/A'}</span></div>
      <div class="order-detail-item"><span class="label">PT Thanh toán:</span><span class="value"><span class="badge badge-info">${o.PaymentMethod || 'COD'}</span></span></div>
      <div class="order-detail-item"><span class="label">Trạng thái:</span><span class="value">
        ${(() => {
          let s = (o.Status || o.status || 'Pending').toLowerCase();
          let key = 'pending';
          if (s.includes('pending') || s.includes('chờ')) key = 'pending';
          else if (s.includes('confirm') || s.includes('xác nhận')) key = 'confirmed';
          else if (s.includes('shipping') || s.includes('đang giao')) key = 'shipping';
          else if (s.includes('delivered') || s.includes('đã giao') || s.includes('complete') || s.includes('hoàn thành')) key = 'delivered';
          else if (s.includes('cancel') || s.includes('hủy')) key = 'cancelled';
          return `<span class="badge ${key === 'delivered' ? 'badge-success' : (key === 'cancelled' ? 'badge-danger' : 'badge-warning')}">${t('opt-' + key)}</span>`;
        })()}
      </span></div>
    </div>

    <h4 style="margin: 24px 0 12px; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 1px; color: var(--text-secondary); border-bottom: 1px solid var(--border); padding-bottom: 8px;">Dòng sản phẩm</h4>

    <table class="order-items-table">
      <thead>
        <tr>
          <th>Sản phẩm</th>
          <th style="text-align:center;">Size / Màu</th>
          <th style="text-align:center;">SL</th>
          <th style="text-align:right;">Đơn giá</th>
        </tr>
      </thead>
      <tbody>
        ${itemsHtml || '<tr><td colspan="4" class="loading-cell">Không có dữ liệu sản phẩm chi tiết</td></tr>'}
      </tbody>
    </table>

    <div style="margin-top: 24px; padding: 20px; background: var(--bg-hover); border-radius: var(--radius); display: flex; justify-content: space-between; align-items: center;">
      <span style="font-weight:600; color:var(--text-secondary);">TỔNG GIÁ TRỊ ĐƠN HÀNG</span>
      <span style="font-size:1.4rem; font-weight:800; color:var(--maroon);">${fmt(o.TotalAmount || o.totalAmount)}</span>
    </div>
  `;

  // Set the current status in the update dropdown
  const statusSelect = $('order-status-update');
  if (statusSelect) {
    statusSelect.value = (o.Status || o.status || 'pending').toLowerCase();
  }

  // Store current order ID for the update function
  window.currentViewingOrderId = id;

  showModal('order-modal');
}

window.updateOrderStatus = async function(id, s) {
  // If called from UI without args, use modal state (matches Android Admin App logic)
  if (!id) id = window.currentViewingOrderId;
  if (!s) s = $('order-status-update')?.value;
  if (!id || !s) return;

  try {
    await db.ref(`Orders/${id}`).update({ Status: s });
    showToast('Cập nhật trạng thái thành công!', 'success');
    closeModal('order-modal');
    loadData();
  } catch (err) {
    showToast('Lỗi: ' + err.message, 'error');
  }
}

let currentOrderItems = [];

window.openOrderCreateModal = function() {
  $('new-order-FullName').value = '';
  $('new-order-Phone').value = '';
  $('new-order-Email').value = '';
  $('new-order-ShippingAddress').value = '';
  currentOrderItems = [];
  renderNewOrderItems();
  $('new-order-TotalAmount').value = '0';
  $('new-order-PaymentMethod').value = 'COD';
  $('new-order-Note').value = '';

  // Populate product select
  const select = $('new-order-product-select');
  if (select) {
    select.innerHTML = '<option value="">-- Chọn sản phẩm --</option>' +
      state.products.map(p => `<option value="${p.id}">${getLocalizedProp(p, 'ProductName')} (${fmt(p.Price || p.price)})</option>`).join('');
  }

  showModal('order-create-modal');
}

window.addOrderItem = function() {
  const pId = $('new-order-product-select').value;
  const size = $('new-order-item-size').value.trim();
  const color = $('new-order-item-color').value.trim();
  const qty = parseInt($('new-order-item-qty').value) || 1;

  if (!pId) {
    showToast('Vui lòng chọn sản phẩm!', 'warning');
    return;
  }

  const p = state.products.find(x => x.id === pId);
  if (!p) return;

  // Check if same item exists (same product, size, color)
  const existing = currentOrderItems.find(i => i.ProductId === pId && i.SelectedSize === size && i.SelectedColor === color);
  if (existing) {
    existing.Quantity += qty;
  } else {
    currentOrderItems.push({
      ProductId: pId,
      ProductName: getLocalizedProp(p, 'ProductName'),
      Price: p.Price || p.price,
      Quantity: qty,
      SelectedSize: size,
      SelectedColor: color,
      ImageUrl: getProductImage(p)
    });
  }

  renderNewOrderItems();

  // Reset inputs
  $('new-order-item-size').value = '';
  $('new-order-item-color').value = '';
  $('new-order-item-qty').value = '1';
}

window.removeOrderItem = function(index) {
  currentOrderItems.splice(index, 1);
  renderNewOrderItems();
}

function renderNewOrderItems() {
  const tbody = $('new-order-items-list');
  if (!tbody) return;

  if (currentOrderItems.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 15px; color: var(--text-muted);">Chưa có sản phẩm nào được chọn</td></tr>';
    $('new-order-TotalAmount').value = 0;
    return;
  }

  let total = 0;
  tbody.innerHTML = currentOrderItems.map((item, index) => {
    const lineTotal = item.Price * item.Quantity;
    total += lineTotal;
    return `
      <tr>
        <td>${item.ProductName}</td>
        <td style="text-align:center;">${item.SelectedSize}/${item.SelectedColor}</td>
        <td style="text-align:center;">${item.Quantity}</td>
        <td style="text-align:right;">${fmt(item.Price)}</td>
        <td style="text-align:center;">
          <button type="button" class="btn-icon" onclick="removeOrderItem(${index})">✕</button>
        </td>
      </tr>
    `;
  }).join('');

  $('new-order-TotalAmount').value = total;
}

window.saveOrder = async function() {
  if (currentOrderItems.length === 0) {
    alert('Vui lòng thêm ít nhất một sản phẩm vào đơn hàng!');
    return;
  }

  const data = {
    FullName: $('new-order-FullName').value,
    Phone: $('new-order-Phone').value,
    Email: $('new-order-Email').value,
    ShippingAddress: $('new-order-ShippingAddress').value,
    Items: currentOrderItems,
    ItemsSummary: currentOrderItems.map(i => `${i.Quantity}x ${i.ProductName} (${i.SelectedSize}/${i.SelectedColor})`).join(' | '),
    TotalAmount: Number($('new-order-TotalAmount').value),
    PaymentMethod: $('new-order-PaymentMethod').value,
    Note: $('new-order-Note').value,
    Status: 'Pending',
    CreatedAt: Date.now()
  };

  if (!data.FullName || !data.Phone || !data.ShippingAddress) {
    alert('Vui lòng nhập đầy đủ thông tin bắt buộc (*)');
    return;
  }

  const btn = document.querySelector('#order-create-modal .btn-primary');
  btn.disabled = true;
  btn.textContent = 'Đang xử lý...';

  try {
    // 1. Dùng transaction để trừ kho cho từng sản phẩm và variant
    const stockUpdates = currentOrderItems.map(async (item) => {
      // Cập nhật tổng tồn kho
      const totalStockRef = db.ref(`Products/${item.ProductId}/StockQuantity`);
      const totalStockPromise = totalStockRef.transaction((currentStock) => {
        if (currentStock === null) return 0;
        return (currentStock || 0) - item.Quantity;
      });

      // Cập nhật tồn kho của Variant cụ thể (mẫu mới)
      const variantKey = `${item.SelectedSize}_${item.SelectedColor}`;
      const variantStockRef = db.ref(`Products/${item.ProductId}/Variants/${variantKey}/Stock`);
      const variantStockPromise = variantStockRef.transaction((currentStock) => {
        if (currentStock === null) return 0;
        return (currentStock || 0) - item.Quantity;
      });

      // Cập nhật tồn kho theo cấu trúc cũ (Stock/Size/Color)
      const oldStockRef = db.ref(`Products/${item.ProductId}/Stock/${item.SelectedSize}/${item.SelectedColor}`);
      const oldStockPromise = oldStockRef.transaction((currentStock) => {
        if (currentStock === null) return 0;
        return (currentStock || 0) - item.Quantity;
      });

      return Promise.all([totalStockPromise, variantStockPromise, oldStockPromise]);
    });

    await Promise.all(stockUpdates);

    // 2. Lưu đơn hàng
    const nextId = getNextId(state.orders, 'ORD', 1001);
    await db.ref(`Orders/${nextId}`).set(data);

    showToast('Tạo đơn hàng thành công và đã cập nhật tồn kho!', 'success');
    closeModal('order-create-modal');
    loadData();
  } catch (e) {
    alert('Lỗi: ' + e.message);
  } finally {
    btn.disabled = false;
    btn.textContent = t('btn-save');
  }
}

window.openUserModal = function() {
  $('user-id-field').value = '';
  $('user-Email').value = '';
  $('user-DisplayName').value = '';
  $('user-Password').value = '';
  $('user-Phone').value = '';
  showModal('user-modal');
}

window.saveUser = async function() {
  const id = $('user-id-field').value;
  const data = {
    Email: $('user-Email').value,
    DisplayName: $('user-DisplayName').value,
    Phone: $('user-Phone').value,
    DateOfBirth: $('user-DateOfBirth').value,
    Gender: $('user-Gender').value,
    Role: $('user-Role').value,
    IsActive: $('user-IsActive').value === 'true',
    UpdatedAt: Date.now()
  };
  if ($('user-Password').value) data.Password = $('user-Password').value;
  if (!id) data.CreatedAt = Date.now();

  try {
    if (id) {
      await db.ref(`Users/${id}`).update(data);
    } else {
      const nextId = getNextId(state.users, 'USR', 1);
      await db.ref(`Users/${nextId}`).set(data);
    }
    closeModal('user-modal');
    loadData();
  } catch (e) { alert(e.message); }
}

// ── Users ──────────────────────────────────────────────
function renderUsers() {
  const tbody = $('users-table-body');
  if (!tbody) return;
  tbody.innerHTML = state.users.map(u => {
    const dateStr = getISODate(u.CreatedAt || u.createdAt);
    return `
    <tr data-date="${dateStr}">
      <td><div class="avatar-placeholder">${(u.DisplayName || u.Email || '?').charAt(0).toUpperCase()}</div></td>
      <td style="font-weight:600;">${u.DisplayName || 'N/A'}</td>
      <td>${u.Email || 'N/A'}</td>
      <td>${u.DateOfBirth || '—'}</td>
      <td>${u.Gender || '—'}</td>
      <td><span class="badge badge-accent">${u.Role || 'user'}</span></td>
      <td><span class="badge ${u.IsActive ? 'badge-success' : 'badge-danger'}">${u.IsActive ? 'Hoạt động' : 'Bị khóa'}</span></td>
      <td>${fmtDate(u.CreatedAt || u.createdAt)}</td>
      <td style="text-align:right;">
        <div class="action-btns">
          <button class="btn-icon btn-icon-edit" onclick="editUser('${u.id}')">✏️</button>
          <button class="btn-icon btn-icon-delete" onclick="deleteNode('Users','${u.id}')">🗑️</button>
        </div>
      </td>
    </tr>
  `}).join('');
  if (window.filterUsers) window.filterUsers();
}

window.editUser = function(id) {
  const u = state.users.find(x => x.id === id);
  if (!u) return;
  $('user-id-field').value = u.id;
  $('user-Email').value = u.Email || '';
  $('user-DisplayName').value = u.DisplayName || '';
  $('user-Phone').value = u.Phone || '';
  $('user-DateOfBirth').value = u.DateOfBirth || '';
  $('user-Gender').value = u.Gender || '';
  $('user-Role').value = u.Role || 'user';
  $('user-IsActive').value = String(u.IsActive);
  showModal('user-modal');
}

// ── Employees ──────────────────────────────────────────
function renderEmployees() {
  $('employees-table-body').innerHTML = state.employees.map(e => `
    <tr>
      <td><div style="font-weight:600">${e.FullName || 'N/A'}</div><div style="font-size:0.7rem;color:var(--text-muted)">ID: ${e.id.substring(0,8)}</div></td>
      <td><span class="badge badge-info">${e.Position || 'N/A'}</span></td>
      <td>${e.Department || 'N/A'}</td>
      <td>${e.Email || 'N/A'}</td>
      <td><span class="badge ${e.IsActive ? 'badge-success' : 'badge-danger'}">${e.IsActive ? 'Active' : 'Inactive'}</span></td>
      <td style="text-align:right;">
        <div class="action-btns">
          <button class="btn-icon btn-icon-edit" onclick="editEmployee('${e.id}')">✏️</button>
          <button class="btn-icon btn-icon-delete" onclick="deleteNode('Employees','${e.id}')">🗑️</button>
        </div>
      </td>
    </tr>
  `).join('');
}

window.openEmployeeModal = () => {
  $('emp-id-field').value = '';
  $('emp-name').value = '';
  $('emp-email').value = '';
  $('emp-phone').value = '';
  $('emp-position').value = '';
  $('emp-department').value = '';
  showModal('employee-modal');
}

window.editEmployee = (id) => {
  const e = state.employees.find(x => x.id === id);
  if (!e) return;
  $('emp-id-field').value = e.id;
  $('emp-name').value = e.FullName || '';
  $('emp-email').value = e.Email || '';
  $('emp-phone').value = e.Phone || '';
  $('emp-position').value = e.Position || '';
  $('emp-department').value = e.Department || '';
  showModal('employee-modal');
}

window.saveEmployee = async () => {
  const id = $('emp-id-field').value;
  const data = {
    FullName: $('emp-name').value,
    Email: $('emp-email').value,
    Phone: $('emp-phone').value,
    Position: $('emp-position').value,
    Department: $('emp-department').value,
    IsActive: true,
    UpdatedAt: Date.now()
  };
  if (!id) data.CreatedAt = Date.now().toString();

  try {
    if (id) {
      await db.ref(`Employees/${id}`).update(data);
    } else {
      const nextId = getNextId(state.employees, 'EMP', 1);
      await db.ref(`Employees/${nextId}`).set(data);
    }
    closeModal('employee-modal');
    loadData();
  } catch (e) { alert(e.message); }
}

// ── Blogs ──────────────────────────────────────────────
function renderBlogs() {
  const tbody = $('blogs-table-body');
  if (!tbody) return;

  tbody.innerHTML = state.blogs.map(b => {
    const blogImg = b.CoverImage || b.image || b.imageUrl || NO_IMAGE_SVG;
    const createdAt = b.CreatedAt || b.createdAt || b.timestamp;
    const dateStr = getISODate(createdAt);
    return `
      <tr data-date="${dateStr}">
        <td><img src="${blogImg}" class="thumb" onerror="this.src='${NO_IMAGE_SVG}'"></td>
        <td style="font-weight:600;">${getLocalizedProp(b, 'Title') || 'Không tiêu đề'}</td>
        <td>${b.Author || b.author || 'Admin'}</td>
        <td><div style="max-width:120px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${b.Tags || b.tags || '—'}</div></td>
        <td><span class="badge ${b.IsPublished || b.status === 'Published' ? 'badge-success' : 'badge-muted'}">${b.IsPublished || b.status === 'Published' ? 'Đã đăng' : 'Bản nháp'}</span></td>
        <td style="font-size:0.8rem;">${fmtDate(createdAt)}</td>
        <td style="text-align:right;">
          <div class="action-btns">
             <button class="btn-icon btn-icon-edit" onclick="editBlog('${b.id}')">✏️</button>
             <button class="btn-icon btn-icon-delete" onclick="deleteNode('Blogs','${b.id}')">🗑️</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

// ── Vouchers ───────────────────────────────────────────
function renderVouchers() {
  const tbody = $('vouchers-table-body');
  if (!tbody) return;

  if (!state.vouchers || state.vouchers.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px; color:var(--text-muted);">Chưa có Voucher nào được tạo.</td></tr>';
    return;
  }

  tbody.innerHTML = state.vouchers.map(v => {
    const createdAt = v.createdAt || v.CreatedAt || v.timestamp;
    const dateStr = getISODate(createdAt);
    const code = v.code || v.Code || v.id || v.ID || '—';
    const title = v.discountType || v.DiscountType || v.discount_type || v.discounttype || v.title || v.Title || v.name || v.Name || '—';
    const discount = v.discount || v.Discount || v.value || v.Value || v.discountAmount || '0';
    const expiry = v.endDate || v.EndDate || v.end_date || v.enddate || v.expiryDate || v.ExpiryDate || '—';
    const used = (v.usedCount !== undefined) ? v.usedCount : (v.UsedCount !== undefined ? v.UsedCount : (v.usageCount !== undefined ? v.usageCount : 0));
    const limit = v.usageLimit || v.UsageLimit || v.usage_limit || v.usagelimit || v.limit || v.Limit || '∞';
    const status = v.status || v.Status || (v.IsActive === false ? 'Expired' : 'Active');
    const desc = getLocalizedProp(v, 'Description');

    const titleHtml = `
      <div style="display:flex; flex-direction:column; gap:2px;">
        <span style="font-weight:600; color:var(--text-main);">${title}</span>
        ${desc ? `<span style="font-size:0.75rem; color:var(--text-secondary); line-height:1.2;">${desc}</span>` : ''}
      </div>
    `;

    return `
      <tr data-date="${dateStr}">
        <td style="font-weight:700; color:var(--maroon)">${code}</td>
        <td>${titleHtml}</td>
        <td><span class="badge badge-accent">${discount}</span></td>
        <td>${expiry}</td>
        <td style="text-align:center;">${used} / <span style="color:var(--text-muted)">${limit}</span></td>
        <td><span class="badge ${status === 'Active' || status === 'true' || status === true ? 'badge-success' : 'badge-danger'}">${status}</span></td>
        <td style="font-size:0.8rem;">${fmtDate(createdAt)}</td>
        <td style="text-align:right;">
          <div class="action-btns">
            <button class="btn-icon btn-icon-edit" onclick="editVoucher('${v.id}')">✏️</button>
            <button class="btn-icon btn-icon-delete" onclick="deleteNode('Vouchers','${v.id}')">🗑️</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

window.openVoucherModal = () => {
  $('voucher-id-field').value = '';
  $('voucher-code').value = '';
  $('voucher-title').value = '';
  $('voucher-type').value = 'Percentage';
  $('voucher-discount').value = '';
  $('voucher-expiryDate').value = '';
  $('voucher-description').value = '';
  $('voucher-status').value = 'Active';
  $('voucher-usageCount').value = 0;
  if ($('voucher-usageLimit')) $('voucher-usageLimit').value = 100;
  showModal('voucher-modal');
}

window.editVoucher = (id) => {
  const v = state.vouchers.find(x => x.id === id);
  if (!v) return;
  $('voucher-id-field').value = v.id;
  $('voucher-code').value = v.code || v.Code || '';
  $('voucher-title').value = v.discountType || v.DiscountType || v.discount_type || v.title || v.Title || '';
  $('voucher-type').value = v.type || v.Type || 'Percentage';
  $('voucher-discount').value = v.discount || v.Discount || '';
  $('voucher-expiryDate').value = v.endDate || v.EndDate || v.end_date || v.expiryDate || v.ExpiryDate || '';
  $('voucher-description').value = getLocalizedProp(v, 'Description');
  $('voucher-status').value = v.status || v.Status || 'Active';
  $('voucher-usageCount').value = (v.usedCount !== undefined) ? v.usedCount : (v.UsedCount !== undefined ? v.UsedCount : 0);
  if ($('voucher-usageLimit')) $('voucher-usageLimit').value = v.usageLimit || v.UsageLimit || v.usage_limit || 100;
  showModal('voucher-modal');
}

window.saveVoucher = async () => {
  const id = $('voucher-id-field').value;
  const data = {
    code: $('voucher-code').value.trim().toUpperCase(),
    discountType: $('voucher-title').value.trim(),
    endDate: $('voucher-expiryDate').value.trim(),
    usedCount: Number($('voucher-usageCount').value) || 0,
    usageLimit: Number($('voucher-usageLimit')?.value) || 100,
    type: $('voucher-type').value,
    discount: $('voucher-discount').value.trim(),
    description: $('voucher-description').value.trim(),
    status: $('voucher-status').value,
    updatedAt: Date.now()
  };

  // Đồng bộ cho cả Android (PascalCase) và Web (camelCase)
  data.Code = data.code;
  data.DiscountType = data.discountType;
  data.EndDate = data.endDate;
  data.UsedCount = data.usedCount;
  data.UsageLimit = data.usageLimit;
  data.Title = data.discountType;
  data.title = data.discountType;
  data.Description = data.description;
  data.status = data.status;
  data.Status = data.status;
  data.Discount = data.discount;
  data.Type = data.type;

  if (!id) data.createdAt = Date.now();

  if (!data.code || !data.discountType) {
    alert('Vui lòng nhập Mã và Tiêu đề (Loại giảm giá)!');
    return;
  }

  try {
    if (id) {
      await db.ref(`Vouchers/${id}`).update(data);
    } else {
      const nextId = getNextId(state.vouchers, 'VCH', 1);
      await db.ref(`Vouchers/${nextId}`).set(data);
    }
    closeModal('voucher-modal');
    loadData();
  } catch (e) { alert(e.message); }
}

// ── Reviews ────────────────────────────────────────────
function renderReviews() {
  const tbody = $('reviews-body');
  if (!tbody) return;
  if (!state.reviews || state.reviews.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="loading-cell">Chưa có đánh giá nào.</td></tr>';
    return;
  }
  tbody.innerHTML = state.reviews.map(r => {
    const text = getReviewContent(r);
    const dateKeys = ['Date', 'date', 'CreatedAt', 'createdAt', 'UpdatedAt', 'updatedAt', 'timestamp', 'Value', 'value', 'ReviewDate'];
    let reviewDate = null;
    for (const key of dateKeys) {
      if (r[key] !== undefined && r[key] !== null && r[key] !== '') {
        reviewDate = r[key];
        break;
      }
    }

    if (!reviewDate) {
      for (let k in r) {
        const val = r[k];
        if (typeof val === 'string' && (val.includes('/') || val.includes('-')) && val.length >= 8) {
          reviewDate = val;
          break;
        }
      }
    }

    const dateStr = getISODate(reviewDate);
    const displayedDate = fmtDate(reviewDate);

    const isApproved = r.IsApproved === true || String(r.IsApproved).toLowerCase() === 'true';

    // Sửa lỗi: Chỉ tìm kiếm khi ID hoặc Name thực sự tồn tại để tránh khớp nhầm giá trị undefined
    const prod = state.products.find(x =>
      (r.ProductId && (x.id === r.ProductId || x.ProductId === r.ProductId)) ||
      (r.productId && (x.id === r.productId || x.ProductId === r.productId)) ||
      (r.ProductName && (x.ProductName === r.ProductName || x.name === r.ProductName))
    );

    const pName = prod ? getLocalizedProp(prod, 'ProductName') : (r.ProductName || r.ProductId || r.productId || 'Sản phẩm');
    return `
    <tr data-status="${isApproved ? 'true' : 'false'}" data-date="${dateStr}">
      <td>${pName}</td>
      <td>${r.UserName || 'Khách'}</td>
      <td><span class="stars">${'★'.repeat(Math.round(r.Rating || 5))}${'☆'.repeat(5 - Math.round(r.Rating || 5))}</span></td>
      <td><div style="max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${text}</div></td>
      <td><span class="badge ${isApproved ? 'badge-success' : 'badge-warning'}">${isApproved ? 'Đã duyệt' : 'Chờ'}</span></td>
      <td>${displayedDate}</td>
      <td style="text-align:right;">
        ${!isApproved ? `<button class="btn-icon btn-icon-edit" onclick="approveReview('${r.id}')" title="Duyệt đánh giá">✅</button>` : ''}
        <button class="btn-icon btn-icon-delete" onclick="deleteNode('Reviews','${r.id}')">🗑️</button>
      </td>
    </tr>
  `}).join('');
  if (window.filterReviews) window.filterReviews();
}

window.approveReview = async (id) => {
  try {
    // Cập nhật cả 2 biến thể để đảm bảo App Android nhận được dù dùng kiểu đặt tên nào
    await db.ref(`Reviews/${id}`).update({
      IsApproved: true,
      isApproved: true
    });
    showToast('Đã duyệt đánh giá!', 'success');
    loadData();
  } catch (e) {
    showToast('Lỗi: ' + e.message, 'error');
  }
}

window.openBlogModal = () => {
  $('blog-id-field').value = '';
  $('blog-Title').value = '';
  $('blog-Author').value = '';
  $('blog-Summary').value = '';
  $('blog-Tags').value = '';
  $('blog-Content').value = '';
  $('blog-CoverImage').value = '';
  $('blog-IsPublished').value = 'true';
  showModal('blog-modal');
}

window.editBlog = function(id) {
  const b = state.blogs.find(x => x.id === id);
  if (!b) return;
  $('blog-id-field').value = b.id;
  $('blog-Title').value = getLocalizedProp(b, 'Title');
  $('blog-Author').value = b.Author || b.author || '';
  $('blog-IsPublished').value = String(b.IsPublished);
  $('blog-CoverImage').value = b.CoverImage || b.image || '';
  $('blog-Tags').value = b.Tags || '';
  $('blog-Summary').value = getLocalizedProp(b, 'Summary');
  $('blog-Content').value = getLocalizedProp(b, 'Content');
  showModal('blog-modal');
}

window.saveBlog = async () => {
  const id = $('blog-id-field').value;
  const data = {
    Title: $('blog-Title').value,
    title: $('blog-Title').value, // compatibility
    Author: $('blog-Author').value,
    author: $('blog-Author').value, // compatibility
    IsPublished: $('blog-IsPublished').value === 'true',
    status: $('blog-IsPublished').value === 'true' ? 'Published' : 'Draft',
    CoverImage: $('blog-CoverImage').value,
    image: $('blog-CoverImage').value, // compatibility
    Tags: $('blog-Tags').value,
    Summary: $('blog-Summary').value,
    Content: $('blog-Content').value,
    content: $('blog-Content').value, // compatibility
    UpdatedAt: Date.now()
  };
  if (!id) data.CreatedAt = Date.now();

  try {
    if (id) {
      await db.ref(`Blogs/${id}`).update(data);
    } else {
      const nextId = getNextId(state.blogs, 'BLG', 1);
      await db.ref(`Blogs/${nextId}`).set(data);
    }
    closeModal('blog-modal');
    loadData();
  } catch (e) { alert(e.message); }
}

// ── Settings ───────────────────────────────────────────
function renderSettings() {
  const s = state.settings;
  $('set-shop-name').value = s.shopName || 'SAIVE Luxury Store';
  $('set-shipping').value = s.shippingFee || 30000;
  $('set-min-free').value = s.minFreeShipping || 1000000;
  $('set-address').value = s.address || '227 Nguyễn Văn Cừ, Quận 5, TP.HCM';
  $('set-phone').value = s.phone || '0901 234 567';
  $('set-email').value = s.email || 'contact@saive.vn';

  // UI preferences from localStorage
  $('set-theme').value = localStorage.getItem('admin-theme') || 'dark';
  $('set-language').value = localStorage.getItem('admin-lang') || 'vi';
}

window.saveSettings = async () => {
  const data = {
    shopName: $('set-shop-name').value,
    shippingFee: $('set-shipping').value,
    minFreeShipping: $('set-min-free').value,
    address: $('set-address').value,
    phone: $('set-phone').value,
    email: $('set-email').value
  };
  await db.ref('Settings').set(data);
  alert('Đã cập nhật cấu hình hệ thống!');
}

// ── Global Delete ──────────────────────────────────────
window.deleteNode = async function(node, id) {
  if (confirm(t('confirm-delete'))) {
    await db.ref(`${node}/${id}`).remove();
    loadData();
  }
}

// ── Theme & Language ─────────────────────────────────
window.changeTheme = function(theme) {
  document.body.setAttribute('data-theme', theme);
  localStorage.setItem('admin-theme', theme);
  // Re-render charts to apply theme colors if analytics is open
  if (wordCloudChart || treemapChart) {
    initAnalyticsCharts();
  }
  // Also refresh dashboard charts if we are on dashboard
  if (state.currentPage === 'dashboard') {
    renderStats();
  }
};

window.changeLanguage = function(lang) {
  localStorage.setItem('admin-lang', lang);
  location.reload(); // Simple way to apply language change if using a translation map
};

// Apply saved theme on load
const savedTheme = localStorage.getItem('admin-theme') || 'dark';
document.body.setAttribute('data-theme', savedTheme);

// ── Auth Logic ───────────────────────────────────────
window.handleLoginSubmit = async function() {
  const email = $('login-email').value.trim();
  const pass = $('login-password').value;
  const errorEl = $('login-error');
  const btn = document.querySelector('.btn-login');

  if (!email || !pass) {
    showToast(currentLang() === 'en' ? 'Please enter all fields!' : 'Vui lòng nhập đủ thông tin!', 'error');
    return;
  }

  btn.disabled = true;
  btn.textContent = currentLang() === 'en' ? 'Authenticating...' : 'Đang xác thực...';

  try {
    console.log("Attempting login for:", email);

    // 1. Tìm User theo Email (thử cả 2 trường hợp viết hoa/thường của Key Email)
    let snapshot = await db.ref('Users').orderByChild('Email').equalTo(email).once('value');
    if (!snapshot.exists()) {
      snapshot = await db.ref('Users').orderByChild('email').equalTo(email).once('value');
    }

    const userData = snapshot.val();
    if (userData) {
      const uid = Object.keys(userData)[0];
      const user = userData[uid];

      console.log("User found:", user); // Xem log này trong Console (F12) để biết Role thực tế

      const userPass = user.Password || user.password;
      const userRole = user.Role || user.role || user.position || user.Position;
      const userEmail = user.Email || user.email;
      const userName = user.DisplayName || user.displayName || user.FullName || user.fullname || 'Admin';

      if (userPass === pass) {
        // Chấp nhận mọi biến thể của quyền quản trị
        const isAdmin = ['admin', 'Admin', 'manager', 'Manager', 'owner', 'nhân viên', 'Employee'].some(r =>
          userRole?.toString().toLowerCase().includes(r.toLowerCase())
        );

        if (isAdmin || userRole === 'admin') {
          localStorage.setItem('admin_logged_in', 'true');
          localStorage.setItem('admin_user', JSON.stringify({
            uid: uid,
            email: userEmail,
            name: userName
          }));

          showAdminApp();
          showToast(t('toast-welcome-admin') || 'Chào mừng quay trở lại!', 'success');
        } else {
          console.warn("Access denied. Role:", userRole);
          showToast(t('toast-no-permission') || `Tài khoản quyền '${userRole}' không thể vào Admin!`, 'error');
        }
      } else {
        errorEl.style.display = 'block';
        errorEl.textContent = t('toast-incorrect-pass') || 'Mật khẩu không chính xác!';
      }
    } else {
      console.error("Email not found in Firebase Users node");
      errorEl.style.display = 'block';
      errorEl.textContent = t('toast-email-not-found') || 'Không tìm thấy tài khoản này!';
    }
  } catch (error) {
    console.error("Login Error:", error);
    showToast(currentLang() === 'en' ? 'System error during login!' : 'Lỗi hệ thống khi đăng nhập!', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = t('login-btn');
    if (errorEl.style.display === 'block') {
      setTimeout(() => { errorEl.style.display = 'none'; }, 4000);
    }
  }
}

window.handleLogout = function() {
  if (confirm(t('confirm-logout'))) {
    localStorage.removeItem('admin_logged_in');
    location.reload();
  }
}

function showAdminApp() {
  $('login-screen').style.display = 'none';
  $('admin-app').style.display = 'block';
  applyTranslations();
  loadData();
}

// Init
applyTranslations();
const isLoggedIn = localStorage.getItem('admin_logged_in') === 'true';
if (isLoggedIn) {
  showAdminApp();
} else {
  $('login-screen').style.display = 'flex';
  $('admin-app').style.display = 'none';
}

setInterval(() => {
  if (localStorage.getItem('admin_logged_in') === 'true') {
    loadData();
  }
}, 30000);

// Add event listener for nav items that might not be handled by window.navigate
document.querySelectorAll('.nav-item').forEach(item => {
  item.addEventListener('click', (e) => {
    e.preventDefault();
    const page = item.getAttribute('data-page');
    if (page) navigate(page);
  });
});

// ── Search & Filters ──────────────────────────────────
window.filterProducts = () => {
  const query = normalizeStr($('product-search').value);
  const rows = document.querySelectorAll('#products-table-body tr');
  rows.forEach(row => {
    const text = normalizeStr(row.innerText);
    row.style.display = text.includes(query) ? '' : 'none';
  });
};

window.filterOrders = () => {
  const query = normalizeStr($('order-search').value);
  const status = $('order-status-filter').value.toLowerCase();
  const dateFrom = $('order-date-from').value; // YYYY-MM-DD
  const dateTo = $('order-date-to').value;     // YYYY-MM-DD

  const rows = document.querySelectorAll('#orders-table-body tr');
  rows.forEach(row => {
    const text = normalizeStr(row.innerText);
    const rowStatus = row.dataset.status;
    const rowDateStr = row.dataset.date;

    const matchesQuery = text.includes(query);
    const matchesStatus = status === '' || rowStatus === status;

    let matchesDate = true;
    if (rowDateStr) {
      if (dateFrom && rowDateStr < dateFrom) matchesDate = false;
      if (dateTo && rowDateStr > dateTo) matchesDate = false;
    } else if (dateFrom || dateTo) {
      matchesDate = false;
    }

    row.style.display = (matchesQuery && matchesStatus && matchesDate) ? '' : 'none';
  });
};

window.filterUsers = () => {
  const query = normalizeStr($('user-search').value);
  const dateFrom = $('user-date-from').value;
  const dateTo = $('user-date-to').value;

  const rows = document.querySelectorAll('#users-table-body tr');
  rows.forEach(row => {
    const text = normalizeStr(row.innerText);
    const dateStr = row.dataset.date;

    let visible = text.includes(query);
    if (dateStr) {
      if (dateFrom && dateStr < dateFrom) visible = false;
      if (dateTo && dateStr > dateTo) visible = false;
    } else if (dateFrom || dateTo) {
      visible = false;
    }

    row.style.display = visible ? '' : 'none';
  });
};

window.filterBlogs = () => {
  const query = normalizeStr($('blog-search').value);
  const dateFrom = $('blog-date-from').value;
  const dateTo = $('blog-date-to').value;

  const rows = document.querySelectorAll('#blogs-table-body tr');
  rows.forEach(row => {
    const text = normalizeStr(row.innerText);
    const dateStr = row.dataset.date;

    let visible = text.includes(query);
    if (dateStr) {
      if (dateFrom && dateStr < dateFrom) visible = false;
      if (dateTo && dateStr > dateTo) visible = false;
    } else if (dateFrom || dateTo) {
      visible = false;
    }

    row.style.display = visible ? '' : 'none';
  });
};

window.filterVouchers = () => {
  const query = normalizeStr($('voucher-search').value);
  const dateFrom = $('voucher-date-from').value;
  const dateTo = $('voucher-date-to').value;

  const rows = document.querySelectorAll('#vouchers-table-body tr');
  rows.forEach(row => {
    const text = normalizeStr(row.innerText);
    const dateStr = row.dataset.date;

    let visible = text.includes(query);
    if (dateStr) {
      if (dateFrom && dateStr < dateFrom) visible = false;
      if (dateTo && dateStr > dateTo) visible = false;
    } else if (dateFrom || dateTo) {
      visible = false;
    }

    row.style.display = visible ? '' : 'none';
  });
};

window.filterReviews = () => {
  const query = normalizeStr($('review-search').value);
  const statusFilter = $('review-approved-filter').value;
  const dateFrom = $('review-date-from').value;
  const dateTo = $('review-date-to').value;

  const rows = document.querySelectorAll('#reviews-body tr');
  rows.forEach(row => {
    const text = normalizeStr(row.innerText);
    const status = row.getAttribute('data-status');
    const date = row.getAttribute('data-date');

    let visible = text.includes(query);
    if (statusFilter && status !== statusFilter) visible = false;
    if (dateFrom && date < dateFrom) visible = false;
    if (dateTo && date > dateTo) visible = false;

    row.style.display = visible ? '' : 'none';
  });
};
