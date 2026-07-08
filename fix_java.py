import os

java_dir = 'app/src/main/java/com/example/saive/admin'
files_to_check = [
    "BlogDetailFragment.java",
    "BlogsFragment.java",
    "LoginActivity.java",
    "ProductDetailFragment.java",
    "ProductEditFragment.java"
]

replacements = {
    '"Edit Blog"': 'getString(R.string.admin_blog_edit)',
    '"Delete Blog"': 'getString(R.string.admin_blog_delete)',
    '"Are you sure you want to delete this blog?"': 'getString(R.string.admin_blog_delete_confirm)',
    '"Tạo bài viết mới"': 'getString(R.string.admin_blog_create)',
    '"Không có phân loại hàng"': 'getString(R.string.admin_product_detail_no_variants)',
    '"Thêm biến thể mới"': 'getString(R.string.admin_product_edit_add_new_variant)',
    '"Xóa sản phẩm"': 'getString(R.string.admin_product_edit_delete_title)',
    '"Bạn có chắc chắn muốn xóa sản phẩm này?"': 'getString(R.string.admin_product_edit_delete_confirm)',
}

for root, _, files in os.walk(java_dir):
    for file in files:
        if file in files_to_check:
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            for k, v in replacements.items():
                content = content.replace(k, v)
            with open(path, 'w', encoding='utf-8') as f:
                f.write(content)
                
print("Java files updated.")
