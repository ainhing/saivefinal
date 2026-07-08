"""
Saive Admin - Reviews Topic Modeling Script (LDA & BERTopic comparison)
========================================================================
This script pulls reviews from Saive's Firebase Realtime Database, cleans the text,
and applies Latent Dirichlet Allocation (LDA) to classify review texts into topics.

Requirements:
    pip install requests scikit-learn numpy

To Run:
    python topic_analysis.py
"""

import requests
import json
import re

# Firebase configuration
FIREBASE_URL = "https://saive-403f7-default-rtdb.asia-southeast1.firebasedatabase.app/Reviews.json"

# Common Vietnamese stop words
STOP_WORDS = {
    "và", "có", "là", "thì", "mà", "của", "được", "cho", "trong", "ra", "lại", "ở", "bị", "cái", "này", "như", "nó", "đây", "đó", 
    "với", "các", "những", "một", "nhưng", "cũng", "đã", "đang", "sẽ", "đi", "về", "lên", "xuống", "vào", "đến", 
    "nhiều", "ít", "quá", "rất", "khá", "để", "nếu", "khi", "vì", "nên", "tự", "mình", "họ", "ta", "tôi", "bạn", 
    "nha", "ạ", "nhé", "nhe", "ấy", "đều", "hơn", "nhất", "không", "ko", "k", "khg", "chưa", "rồi", "nữa",
    "cực", "kỳ", "thật", "luôn", "thấy", "lại", "còn", "chỉ", "sự", "việc", "sản", "phẩm", "sp", "hàng", "cửa", "hàng"
}

def clean_vietnamese_text(text):
    if not text:
        return ""
    text = text.lower()
    # Remove punctuation
    text = re.sub(r'[^\w\s]', ' ', text)
    # Split and remove stop words and short words
    words = [w for w in text.split() if w not in STOP_WORDS and len(w) > 2 and not w.isdigit()]
    return " ".join(words)

def main():
    print("1. Fetching reviews from Firebase Database...")
    try:
        response = requests.get(FIREBASE_URL)
        if response.status_code != 200:
            print(f"Error fetching data: HTTP {response.status_code}")
            return
        
        reviews_data = response.json()
        if not reviews_data:
            print("No reviews found in the database.")
            return
        
        print(f"Loaded {len(reviews_data)} review records.")
    except Exception as e:
        print(f"Connection failed: {e}")
        return

    # Extract contents
    raw_texts = []
    for key, item in reviews_data.items():
        content = item.get("Content") or item.get("Comment") or item.get("content") or item.get("comment")
        if content:
            raw_texts.append(content)
            
    print(f"Extracted {len(raw_texts)} non-empty review texts.")
    
    # Preprocess texts
    cleaned_texts = [clean_vietnamese_text(t) for t in raw_texts]
    cleaned_texts = [t for t in cleaned_texts if t]  # Filter empty strings

    if len(cleaned_texts) < 5:
        print("Too few reviews to perform meaningful topic modeling. Needs at least 5 reviews.")
        return

    print("\n2. Running LDA (Latent Dirichlet Allocation) Topic Modeling...")
    from sklearn.feature_extraction.text import CountVectorizer
    from sklearn.decomposition import LatentDirichletAllocation
    
    # Vectorize texts
    vectorizer = CountVectorizer(max_df=0.95, min_df=1, token_pattern=r'\b\w+\b')
    tf = vectorizer.fit_transform(cleaned_texts)
    feature_names = vectorizer.get_feature_names_out()

    # We model 4 topics matching the core areas: PRODUCT, PRICE, SERVICE, DELIVERY
    n_topics = 4
    lda = LatentDirichletAllocation(n_components=n_topics, random_state=42, max_iter=20)
    lda.fit(tf)

    print("\n=== Topics Discovered by LDA ===")
    topics = {}
    for topic_idx, topic in enumerate(lda.components_):
        top_features_ind = topic.argsort()[:-11:-1]
        top_words = [feature_names[i] for i in top_features_ind]
        print(f"\nTopic #{topic_idx + 1}:")
        print("  Words: " + ", ".join(top_words))
        
        # Determine closest mapping to predefined categories based on overlapping seed words
        product_seeds = {"chất", "lượng", "đẹp", "vừa", "vải", "bền", "size", "thiết", "kế", "màu"}
        price_seeds = {"giá", "rẻ", "tiền", "đắt", "worth", "đáng", "chi", "phí"}
        service_seeds = {"shop", "tư", "vấn", "nhiệt", "tình", "rep", "phản", "hồi", "hỗ", "trợ"}
        delivery_seeds = {"giao", "ship", "nhanh", "đóng", "gói", "hộp", "shipper", "chậm", "trễ"}
        
        overlap_scores = {
            "PRODUCT": len(set(top_words) & product_seeds),
            "PRICE": len(set(top_words) & price_seeds),
            "SERVICE": len(set(top_words) & service_seeds),
            "DELIVERY": len(set(top_words) & delivery_seeds)
        }
        best_match = max(overlap_scores, key=overlap_scores.get)
        print(f"  Suggested Category: {best_match} (overlap score: {overlap_scores[best_match]})")
        topics[f"Topic_{topic_idx+1}"] = {
            "words": top_words,
            "category": best_match
        }

    # Summary of methods
    print("\n" + "="*50)
    print("Methodological Comparison for Coursework Report:")
    print("1. LDA (Latent Dirichlet Allocation):")
    print("   - Type: Generative probabilistic model (unsupervised).")
    print("   - Complexity: Low. Runs in seconds on standard CPUs. Easy to deploy in lightweight environments.")
    print("   - Best for: General pattern mining, extracting word bags when computational power is limited.")
    print("2. BERTopic:")
    print("   - Type: Transformer-based embeddings (BERT) + UMAP dimensionality reduction + HDBSCAN clustering.")
    print("   - Complexity: High. Requires substantial disk space (>500MB), CPU/GPU memory, and takes longer to initialize.")
    print("   - Best for: Semantic coherence, contextual understanding, handling short texts like reviews.")
    print("   - Verdict: Due to mobile memory limits, the on-device Java/JavaScript classifier runs in real-time, "
          "while LDA provides the easiest server/offline analysis script.")
    print("="*50)

if __name__ == "__main__":
    main()
