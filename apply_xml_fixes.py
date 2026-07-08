import os
import re

mapping = {
    "KIỂM DUYỆT ĐÁNH GIÁ": "admin_review_mgmt_title",
    "Kích thước (ví dụ: M, L, XL)": "admin_add_variant_size_hint",
    "Màu sắc (ví dụ: Black, Brown)": "admin_add_variant_color_hint",
    "Số lượng kho": "admin_add_variant_stock_hint",
    "User Details": "admin_user_detail_title",
    "Name": "admin_user_detail_name",
    "EMAIL": "admin_user_detail_email",
    "ROLE": "admin_user_detail_role",
    "AUTH PROVIDER": "admin_user_detail_auth",
    "CREATED AT": "admin_user_detail_created",
    "Close": "admin_user_detail_close",
    "PRODUCT": "admin_dashboard_product",
    "PRICE": "admin_dashboard_price",
    "SERVICE": "admin_dashboard_service",
    "DELIVERY": "admin_dashboard_delivery",
    "Phân loại hàng": "admin_product_detail_variants",
    "Đổi ảnh": "admin_product_edit_change_image",
    "Tên sản phẩm": "admin_product_edit_name",
    "Danh mục": "admin_product_edit_category",
    "Giá gốc": "admin_product_edit_price",
    "Giá sale": "admin_product_edit_sale_price",
    "Số lượng trong kho (Tổng)": "admin_product_edit_total_stock",
    "Quản lý kho theo biến thể (Size/Màu)": "admin_product_edit_manage_variants",
    "Thêm biến thể": "admin_product_edit_add_variant",
    "Đang kinh doanh": "admin_product_edit_is_active",
    "Nổi bật (Featured)": "admin_product_edit_featured",
    "LƯU SẢN PHẨM": "admin_product_edit_save",
    "XÓA SẢN PHẨM": "admin_product_edit_delete",
    "Swipe right to approve, swipe left to reject": "admin_reviews_swipe_hint",
    "SAIVE Admin": "admin_nav_title",
    "Management Console": "admin_nav_subtitle"
}

layout_dir = 'app/src/main/res/layout'
pattern_text = re.compile(r'(android:(text|hint))="([^@].*?)"')
xmls_to_modify = set()

# First, collect all xml files that need modification
with open('hardcoded_results.txt', 'r', encoding='utf-8') as f:
    for line in f:
        if line.startswith('Total'): continue
        parts = line.strip().split(' - ', 1)
        if len(parts) == 2:
            file_name = parts[0].split(':')[0]
            xmls_to_modify.add(file_name)

new_strings = []

for xml_name in xmls_to_modify:
    filepath = os.path.join(layout_dir, xml_name)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Check if tools namespace is needed
    needs_tools = False

    def replacer(match):
        global needs_tools
        attr_name = match.group(1) # android:text or android:hint
        is_hint = 'hint' in attr_name
        text_val = match.group(3)
        if text_val in mapping:
            key = mapping[text_val]
            if not is_hint: # if it was a hint but mapping applies to text, we still keep android:hint
                return f'{attr_name}="@string/{key}"'
            else:
                return f'android:hint="@string/{key}"'
        else:
            # dummy data -> tools:text (or tools:hint, though usually tools:text is enough for both)
            needs_tools = True
            return f'tools:text="{text_val}"'
            
    new_content = pattern_text.sub(replacer, content)

    if needs_tools and 'xmlns:tools' not in new_content:
        # insert xmlns:tools into the root tag
        new_content = new_content.replace('xmlns:android="http://schemas.android.com/apk/res/android"',
                                          'xmlns:android="http://schemas.android.com/apk/res/android"\n    xmlns:tools="http://schemas.android.com/tools"')

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)

for text, key in mapping.items():
    new_strings.append(f'    <string name="{key}">{text}</string>')

with open('admin_strings_temp.xml', 'w', encoding='utf-8') as f:
    f.write('\n'.join(new_strings))

print("XMLs updated and admin_strings_temp.xml generated.")
