package com.example.saive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.saive.R;
import com.example.saive.adapters.RecommendationAdapter;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.Product;
import com.example.saive.utils.CartManager;
import com.example.saive.utils.DataManager;
import com.example.saive.utils.PriceFormatter;
import com.example.saive.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * RecommendationActivity — "Shop The Look"
 *
 * Dynamic rule engine:
 *  - Detects the TYPE of the source product (top / bottom / dress / outerwear / shoes / bag / accessory)
 *  - Applies a RULE SET matching that type → decides which slot(s) to fill
 *  - Scores every candidate by tag_style + tag_color harmony + type-specific affinity
 *  - Picks the best match per slot (max 2 items)
 *  - Generates a rich, varied explanation per item
 */
public class RecommendationActivity extends BaseActivity {

    // ─── Views ───────────────────────────────────────────────────────────────
    private ImageView btnBack, ivMainProduct;
    private TextView tvMainProductName, tvMainProductPrice, tvMainStyle, tvMainColors, tvSectionLabel;
    private RecyclerView rvRecommendations;
    private View btnAddAllToCart;
    private LinearLayout emptyView;

    // ─── Data ────────────────────────────────────────────────────────────────
    private Product mainProduct;
    private final List<Product>  recommendedList = new ArrayList<>();
    private final List<String[]> reasonList      = new ArrayList<>(); // [title, detail]
    private RecommendationAdapter adapter;

    // ─── Lifecycle ───────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateStatusBar();
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recommendation);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(android.R.id.content), (v, insets) -> {
                androidx.core.graphics.Insets sys = insets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type.systemBars());
                View header = findViewById(R.id.layoutHeader);
                if (header != null) header.setPadding(0, sys.top, 0, 0);
                return insets;
            });

        initViews();
        setupData();
        setupListeners();
    }

    private void updateStatusBar() {
        boolean dark = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        getWindow().setStatusBarColor(
            androidx.core.content.ContextCompat.getColor(this, R.color.colorCotton));
        getWindow().getDecorView().setSystemUiVisibility(
            dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void initViews() {
        btnBack            = findViewById(R.id.btnBack);
        ivMainProduct      = findViewById(R.id.ivMainProduct);
        tvMainProductName  = findViewById(R.id.tvMainProductName);
        tvMainProductPrice = findViewById(R.id.tvMainProductPrice);
        tvMainStyle        = findViewById(R.id.tvMainStyle);
        tvMainColors       = findViewById(R.id.tvMainColors);
        tvSectionLabel     = findViewById(R.id.tvSectionLabel);
        rvRecommendations  = findViewById(R.id.rvRecommendations);
        btnAddAllToCart    = findViewById(R.id.btnAddAllToCart);
        emptyView          = findViewById(R.id.emptyView);
    }

    // ─── Setup ───────────────────────────────────────────────────────────────
    private void setupData() {
        mainProduct = (Product) getIntent().getSerializableExtra("PRODUCT");
        if (mainProduct == null) mainProduct = (Product) getIntent().getSerializableExtra("product");
        if (mainProduct == null) { ToastUtils.showCustomToast(this, "Product not found."); finish(); return; }

        // ── Bind main product UI ──
        tvMainProductName.setText(mainProduct.getName().toUpperCase(Locale.ROOT));
        tvMainProductPrice.setText(PriceFormatter.formatPrice(mainProduct.getPrice()));

        String style = safe(mainProduct.getTagStyle());
        if (!style.isEmpty()) {
            tvMainStyle.setText(style.toUpperCase(Locale.ROOT));
            tvMainStyle.setVisibility(View.VISIBLE);
        }
        List<String> colors = mainProduct.getTagColor();
        if (colors != null && !colors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(colors.size(), 3); i++) {
                if (i > 0) sb.append(" · ");
                sb.append(cap(colors.get(i)));
            }
            tvMainColors.setText(sb.toString());
            tvMainColors.setVisibility(View.VISIBLE);
        }
        loadImage(ivMainProduct, mainProduct);

        // ── Run engine ──
        generateRecommendations();

        // ── Section label ──
        String label = buildSectionLabel(srcGroup());
        if (tvSectionLabel != null) tvSectionLabel.setText(label);

        // ── RecyclerView ──
        adapter = new RecommendationAdapter(recommendedList, reasonList);
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this));
        rvRecommendations.setAdapter(adapter);

        boolean empty = recommendedList.isEmpty();
        if (emptyView != null) emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (btnAddAllToCart != null) btnAddAllToCart.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RECOMMENDATION ENGINE
    // ══════════════════════════════════════════════════════════════════════════

    /** What type/group is the source product? */
    private String srcGroup() {
        String g = safe(mainProduct.getTagTypeGroup());
        if (!g.isEmpty()) return g;
        // Fallback to category
        return safe(mainProduct.getCategory());
    }

    private void generateRecommendations() {
        List<Product> pool = DataManager.getInstance(this).getProducts();
        if (pool == null || pool.isEmpty()) return;

        String src   = srcGroup();
        String style = safe(mainProduct.getTagStyle());
        List<String> srcColors = mainProduct.getTagColor() != null ? mainProduct.getTagColor() : new ArrayList<>();

        /*
         * RULE MATRIX  ─ decides which "slots" to fill based on source type
         *
         * Source      → Slot A             | Slot B
         * ─────────────────────────────────────────────────
         * top         → bottom             | accessory/bag/shoes
         * bottom      → top                | accessory/bag/shoes
         * dress       → outerwear/jacket   | shoes/bag
         * outerwear   → top                | bottom
         * shoes       → bottom             | top
         * bag         → top                | accessory
         * accessory   → top / dress        | bottom / shoes
         * (other)     → top                | bottom
         */
        String slotA, slotB;
        switch (src) {
            case "bottom":   slotA = "top";       slotB = "accessory_broad"; break;
            case "dress":    slotA = "outerwear";  slotB = "shoes_bag";       break;
            case "outerwear":slotA = "top";        slotB = "bottom";          break;
            case "shoes":    slotA = "bottom";     slotB = "top";             break;
            case "bag":      slotA = "top";        slotB = "accessory_small"; break;
            case "accessory":slotA = "outfit_hero";slotB = "shoes_bag";       break;
            default:         slotA = "bottom";     slotB = "accessory_broad"; break; // top/other
        }

        // Score all candidates
        List<ScoredProduct> candidatesA = new ArrayList<>();
        List<ScoredProduct> candidatesB = new ArrayList<>();

        for (Product p : pool) {
            if (isSame(p)) continue;

            String pg     = safe(p.getTagTypeGroup());
            String pStyle = safe(p.getTagStyle());
            List<String> pColors = p.getTagColor() != null ? p.getTagColor() : new ArrayList<>();

            boolean inA = matchesSlot(slotA, pg, safe(p.getCategory()));
            boolean inB = matchesSlot(slotB, pg, safe(p.getCategory()));
            if (!inA && !inB) continue;

            // ── Compute score ──────────────────────────────────────
            int score = 0;
            List<String> signals = new ArrayList<>();

            // 1. Style match (strongest)
            StyleScore ss = scoreStyle(style, pStyle, src, pg);
            score += ss.score;
            if (!ss.reason.isEmpty()) signals.add(ss.reason);

            // 2. Color harmony
            ColorScore cs = scoreColor(srcColors, pColors);
            score += cs.score;
            if (!cs.reason.isEmpty()) signals.add(cs.reason);

            // 3. Type-specific affinity bonus
            TypeScore ts = scoreTypeAffinity(style, src, pg, safe(p.getTagType()), pStyle);
            score += ts.score;
            if (!ts.reason.isEmpty()) signals.add(ts.reason);

            // 4. Small random tiebreaker so same-score items vary each session
            score += new Random().nextInt(5);

            // ── Build reason texts ─────────────────────────────────
            String reasonTitle  = buildTitle(src, pg, style, pStyle, cs);
            String reasonDetail = buildDetail(signals, src, pg, style, pStyle, pColors);

            ScoredProduct sp = new ScoredProduct(p, score, reasonTitle, reasonDetail);
            if (inA) candidatesA.add(sp);
            else     candidatesB.add(sp);
        }

        // Sort descending
        Collections.sort(candidatesA, (a, b) -> b.score - a.score);
        Collections.sort(candidatesB, (a, b) -> b.score - a.score);

        // Pick best per slot
        if (!candidatesA.isEmpty()) {
            ScoredProduct best = candidatesA.get(0);
            recommendedList.add(best.product);
            reasonList.add(new String[]{best.reasonTitle, best.reasonDetail});
        }
        if (!candidatesB.isEmpty()) {
            // Avoid picking the same product as slot A
            for (ScoredProduct sp : candidatesB) {
                if (recommendedList.isEmpty() || !isSameProduct(sp.product, recommendedList.get(0))) {
                    recommendedList.add(sp.product);
                    reasonList.add(new String[]{sp.reasonTitle, sp.reasonDetail});
                    break;
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SLOT MATCHING
    // ══════════════════════════════════════════════════════════════════════════

    private boolean matchesSlot(String slot, String group, String cat) {
        String g = group + " " + cat;
        switch (slot) {
            case "top":
                return g.contains("top") || g.contains("shirt") || g.contains("blouse")
                    || g.contains("tee")  || g.contains("polo") || g.contains("sweater")
                    || g.contains("knit");
            case "bottom":
                return g.contains("bottom") || g.contains("pant") || g.contains("jean")
                    || g.contains("trouser") || g.contains("skirt") || g.contains("short");
            case "outerwear":
                return g.contains("outerwear") || g.contains("jacket") || g.contains("coat")
                    || g.contains("blazer") || g.contains("cardigan");
            case "shoes_bag":
                return g.contains("shoe") || g.contains("boot") || g.contains("sneaker")
                    || g.contains("heel")  || g.contains("bag") || g.contains("purse");
            case "accessory_broad":
                return g.contains("accessory") || g.contains("bag") || g.contains("shoe")
                    || g.contains("belt") || g.contains("glasses") || g.contains("jewel")
                    || g.contains("scarf") || g.contains("hat") || g.contains("watch");
            case "accessory_small":
                return g.contains("accessory") || g.contains("belt") || g.contains("glasses")
                    || g.contains("jewel") || g.contains("scarf") || g.contains("hat")
                    || g.contains("watch");
            case "shoes_only":
                return g.contains("shoe") || g.contains("boot") || g.contains("sneaker") || g.contains("heel");
            case "outfit_hero":  // top or dress
                return g.contains("top") || g.contains("shirt") || g.contains("dress")
                    || g.contains("blouse") || g.contains("knit");
            default:
                return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SCORING MODULES
    // ══════════════════════════════════════════════════════════════════════════

    /** Style compatibility score */
    private StyleScore scoreStyle(String srcStyle, String pStyle, String srcGroup, String pGroup) {
        if (srcStyle.isEmpty() || pStyle.isEmpty()) return new StyleScore(5, "");

        if (srcStyle.equals(pStyle)) {
            return new StyleScore(40, styleMatchPhrase(srcStyle));
        }

        // Compatible style pairs
        String[][] compatPairs = {
            {"minimalist", "classic"}, {"minimalist", "formal"},
            {"casual",     "streetwear"}, {"casual",   "sporty"},
            {"bohemian",   "vintage"}, {"romantic",    "elegant"},
            {"classic",    "formal"},  {"streetwear",  "sporty"},
            {"preppy",     "classic"}, {"luxury",      "formal"},
            {"luxury",     "minimalist"}, {"chic",     "elegant"},
        };
        for (String[] pair : compatPairs) {
            if ((srcStyle.contains(pair[0]) && pStyle.contains(pair[1]))
             || (srcStyle.contains(pair[1]) && pStyle.contains(pair[0]))) {
                return new StyleScore(28, "Phong cách " + cap(pStyle) + " tương thích với " + cap(srcStyle));
            }
        }

        return new StyleScore(8, "");
    }

    private String styleMatchPhrase(String style) {
        switch (style) {
            case "minimalist": return "Cùng tinh thần tối giản — ít là nhiều";
            case "casual":     return "Giữ nguyên vibe thoải mái, dễ mặc";
            case "streetwear": return "DNA streetwear — năng động và cá tính";
            case "formal":     return "Đồng nhất phong cách công sở chuyên nghiệp";
            case "bohemian":   return "Cùng hơi thở boho — tự do và lãng mạn";
            case "romantic":   return "Tông romantic — nhẹ nhàng và nữ tính";
            case "classic":    return "Cổ điển vượt thời gian — không bao giờ lỗi mốt";
            case "vintage":    return "Retro charm — phong cách hoài cổ độc đáo";
            case "luxury":     return "Cùng đẳng cấp luxury — tinh tế từng chi tiết";
            case "sporty":     return "Năng động, trẻ trung cùng phong cách thể thao";
            case "preppy":     return "Preppy schoolboy — gọn gàng và lịch sự";
            default:           return "Cùng phong cách " + cap(style);
        }
    }

    /** Color harmony score */
    private ColorScore scoreColor(List<String> src, List<String> partner) {
        if (src.isEmpty() || partner.isEmpty()) return new ColorScore(5, "");

        // Neutral palette — works with anything
        String[] neutrals = {"black","white","cream","beige","ivory","nude","grey","gray",
                              "navy","tan","camel","off-white","ecru","stone","sand","khaki"};
        // Tone-on-tone pairs (similar family)
        String[][] tonalFamilies = {
            {"black","charcoal","graphite","dark"},
            {"white","cream","ivory","off-white","ecru","beige","sand"},
            {"navy","blue","cobalt","denim"},
            {"brown","tan","camel","cognac","tobacco","mocha"},
            {"green","olive","sage","forest","hunter"},
            {"red","burgundy","wine","maroon","rust"},
            {"pink","blush","rose","dusty rose","mauve"},
            {"grey","gray","silver","ash"},
        };
        // Contrast pairs (complementary)
        String[][] contrastPairs = {
            {"black","white"}, {"navy","cream"}, {"black","cream"},
            {"white","navy"},  {"brown","cream"}, {"olive","cream"},
            {"burgundy","cream"}, {"camel","black"}, {"camel","white"},
            {"grey","burgundy"}, {"dusty rose","grey"}, {"green","cream"},
        };

        // Check for exact tone match
        for (String s : src) {
            for (String p : partner) {
                if (s.equalsIgnoreCase(p))
                    return new ColorScore(35, "Set đồng màu " + cap(p) + " — monochrome cực kỳ sang");
            }
        }

        // Check tonal family
        for (String[] family : tonalFamilies) {
            boolean srcIn = colorInFamily(src, family);
            boolean partIn = colorInFamily(partner, family);
            if (srcIn && partIn) {
                return new ColorScore(28, "Tông màu " + cap(family[0]) + " ăn nhau tạo outfit mượt mà");
            }
        }

        // Check contrast pair
        for (String[] pair : contrastPairs) {
            boolean c1 = colorListContains(src, pair[0]) && colorListContains(partner, pair[1]);
            boolean c2 = colorListContains(src, pair[1]) && colorListContains(partner, pair[0]);
            if (c1 || c2) {
                return new ColorScore(30, "Tương phản " + cap(pair[0]) + " × " + cap(pair[1]) + " — đối lập hài hòa");
            }
        }

        // Check if partner is neutral
        for (String p : partner) {
            for (String n : neutrals) {
                if (p.contains(n))
                    return new ColorScore(20, "Màu " + cap(p) + " trung tính — dễ phối với bất kỳ tông nào");
            }
        }

        return new ColorScore(5, "");
    }

    private boolean colorInFamily(List<String> colors, String[] family) {
        for (String c : colors)
            for (String f : family)
                if (c.toLowerCase(Locale.ROOT).contains(f)) return true;
        return false;
    }

    private boolean colorListContains(List<String> list, String key) {
        for (String c : list) if (c.toLowerCase(Locale.ROOT).contains(key)) return true;
        return false;
    }

    /** Type-specific affinity: special knowledge about what goes well together */
    private TypeScore scoreTypeAffinity(String srcStyle, String srcGroup, String pGroup, String pType, String pStyle) {
        String s = pType + " " + pGroup;

        // ── Source = top ──────────────────────────────────────────
        if (srcGroup.equals("top") || srcGroup.equals("shirt") || srcGroup.equals("blouse")) {
            if (s.contains("trouser") || s.contains("chino") || s.contains("slacks")) {
                return new TypeScore(20, "Quần âu tạo silhouette cân đối, tôn dáng");
            }
            if (s.contains("jean") || s.contains("denim")) {
                return new TypeScore(18, "Classic top + denim — combo không bao giờ sai");
            }
            if (s.contains("maxi") || s.contains("midi") || s.contains("skirt")) {
                return new TypeScore(17, "Phần dưới bồng bềnh cân bằng phần trên fitted");
            }
            if (srcStyle.contains("minimal") && (s.contains("wide") || s.contains("flare"))) {
                return new TypeScore(15, "Wide-leg + minimal top — tỷ lệ đỉnh của chóp");
            }
        }

        // ── Source = bottom ───────────────────────────────────────
        if (srcGroup.contains("bottom") || srcGroup.contains("pant") || srcGroup.contains("skirt")) {
            if (s.contains("crop") || s.contains("short") || pGroup.contains("top")) {
                return new TypeScore(18, "Crop top làm điểm nhấn, tôn set dưới");
            }
            if (s.contains("knit") || s.contains("sweater")) {
                return new TypeScore(17, "Áo len tuck-in tạo vẻ cozy-chic hoàn hảo");
            }
            if (s.contains("blazer") || s.contains("jacket")) {
                return new TypeScore(16, "Blazer over phần trên thêm cấu trúc và đẳng cấp");
            }
        }

        // ── Source = dress ────────────────────────────────────────
        if (srcGroup.contains("dress")) {
            if (s.contains("blazer") || s.contains("jacket") || s.contains("coat")) {
                return new TypeScore(22, "Layer jacket ngoài dress — elegant và linh hoạt");
            }
            if (s.contains("ankle") || s.contains("pump") || s.contains("heel")) {
                return new TypeScore(20, "Heel kéo dài chân, hoàn thiện look nữ tính");
            }
            if (s.contains("sneaker") || s.contains("flat")) {
                return new TypeScore(17, "Sneaker/flat với dress — casual chic thú vị");
            }
            if (s.contains("clutch") || s.contains("mini bag")) {
                return new TypeScore(18, "Mini bag tinh tế, đồng điệu với dress thanh lịch");
            }
        }

        // ── Source = outerwear ────────────────────────────────────
        if (srcGroup.contains("outerwear") || srcGroup.contains("jacket") || srcGroup.contains("coat")) {
            if (s.contains("turtleneck") || s.contains("rollneck")) {
                return new TypeScore(20, "Turtleneck beneath coat — Quiet Luxury thuần chất");
            }
            if (s.contains("straight") || s.contains("slim") || s.contains("trouser")) {
                return new TypeScore(18, "Straight-cut dưới coat — tỷ lệ hoàn hảo");
            }
        }

        // ── Source = shoes ────────────────────────────────────────
        if (srcGroup.contains("shoe") || srcGroup.contains("boot") || srcGroup.contains("sneaker")) {
            if (s.contains("jean") || s.contains("denim")) {
                return new TypeScore(20, "Jeans rolled-up show off đôi giày đẹp");
            }
            if (s.contains("midi") || s.contains("skirt")) {
                return new TypeScore(18, "Midi skirt + boots — combo thu đông iconic");
            }
            if (s.contains("wide") || s.contains("flare")) {
                return new TypeScore(16, "Wide-leg cân bằng khối lượng thị giác với giày");
            }
        }

        // ── Source = bag ──────────────────────────────────────────
        if (srcGroup.contains("bag") || srcGroup.contains("purse")) {
            if (srcStyle.contains("luxury") || srcStyle.contains("formal")) {
                if (s.contains("silk") || s.contains("cashmere") || s.contains("blouse")) {
                    return new TypeScore(20, "Túi hàng hiệu + áo lụa — combo đẳng cấp thực sự");
                }
            }
            if (s.contains("belt") || s.contains("scarf")) {
                return new TypeScore(17, "Belt/scarf nhỏ phối túi tạo bộ accessories hoàn chỉnh");
            }
        }

        // ── Source = accessory ────────────────────────────────────
        if (srcGroup.contains("accessory") || srcGroup.contains("glasses") || srcGroup.contains("belt")) {
            if (pGroup.contains("top") || pGroup.contains("shirt")) {
                return new TypeScore(18, "Outfit tôn sáng accessories, không lấn át nhau");
            }
        }

        return new TypeScore(0, "");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REASON TEXT BUILDERS
    // ══════════════════════════════════════════════════════════════════════════

    private String buildSectionLabel(String srcGroup) {
        switch (srcGroup) {
            case "bottom":    return "HOÀN THIỆN SET TRÊN";
            case "dress":     return "LAYER & ACCESSORIES";
            case "outerwear": return "INNER LOOK";
            case "shoes":     return "XÂY BỘ OUTFIT";
            case "bag":       return "PHỐI CÙNG TÚI";
            case "accessory": return "OUTFIT CÀI ĐIỂM NHẤN";
            default:          return "COMPLETE THE LOOK";
        }
    }

    private String buildTitle(String srcGroup, String pGroup, String srcStyle, String pStyle, ColorScore cs) {
        boolean sameStyle = !srcStyle.isEmpty() && srcStyle.equals(pStyle);

        // Grouped high-quality titles
        String[][] titlePool;
        if (sameStyle && cs.score >= 28) {
            titlePool = new String[][]{
                {"MATCH HOÀN HẢO"},{"SET ĂN Ý"},{"DUO LÝ TƯỞNG"},{"COMBO ĐỈNH"}
            };
        } else if (sameStyle) {
            titlePool = new String[][]{
                {"CÙNG PHONG CÁCH"},{"ĐỒNG ĐIỆU"},{"SAME VIBE"}
            };
        } else if (cs.score >= 25) {
            titlePool = new String[][]{
                {"BỘ ĐÔI MÀU SẮC"},{"TÔNG MÀU HÀI HÒA"},{"COLOR HARMONY"},{"PHỐI MÀU KHÉO"}
            };
        } else {
            // Context-aware fallback by type
            if (pGroup.contains("bottom") || pGroup.contains("pant") || pGroup.contains("skirt")) {
                titlePool = new String[][]{{"GỢI Ý PHẦN DƯỚI"},{"BOTTOM IDEAL"},{"CÂN BẰNG DÁNG"}};
            } else if (pGroup.contains("shoe") || pGroup.contains("boot")) {
                titlePool = new String[][]{{"NÂNG TẦNG BƯỚC CHÂN"},{"SHOE GAME"},{"ĐIỂM NHẤN CUỐI"}};
            } else if (pGroup.contains("bag") || pGroup.contains("purse")) {
                titlePool = new String[][]{{"TÚI HOÀN THIỆN LOOK"},{"CARRY THE LOOK"},{"MINI ACCENT"}};
            } else if (pGroup.contains("outerwear") || pGroup.contains("jacket")) {
                titlePool = new String[][]{{"LAYER ĐẺ LAYER"},{"OUTER LAYER"},{"PHONG CÁCH THÊM TẦNG"}};
            } else {
                titlePool = new String[][]{{"HOÀN THIỆN OUTFIT"},{"GỢI Ý THÊM"},{"STYLE PICK"}};
            }
        }
        return titlePool[new Random().nextInt(titlePool.length)][0];
    }

    private String buildDetail(List<String> signals, String srcGroup, String pGroup,
                               String srcStyle, String pStyle, List<String> pColors) {
        if (!signals.isEmpty()) {
            // Join all signals with bullet separator — max 2 to keep it concise
            StringBuilder sb = new StringBuilder();
            int limit = Math.min(signals.size(), 2);
            for (int i = 0; i < limit; i++) {
                if (i > 0) sb.append("  •  ");
                sb.append(signals.get(i));
            }
            // Optionally append a fashion tip
            String tip = fashionTip(srcGroup, pGroup, srcStyle, pStyle);
            if (!tip.isEmpty() && signals.size() <= 1) {
                sb.append("  •  ").append(tip);
            }
            return sb.toString();
        }

        // Fully generated fallback when no signals
        String colorHint = pColors.isEmpty() ? "" : " màu " + cap(pColors.get(0));
        return generateFallbackReason(srcGroup, pGroup, srcStyle, pStyle, colorHint);
    }

    /** Contextual fashion knowledge tips */
    private String fashionTip(String srcGroup, String pGroup, String srcStyle, String pStyle) {
        // Source = top tips
        if (srcGroup.contains("top") || srcGroup.contains("shirt")) {
            if (pGroup.contains("bottom") || pGroup.contains("pant")) {
                if (srcStyle.contains("oversized") || srcStyle.contains("loose")) {
                    return "Tuck-in hoặc half-tuck làm gọn dáng";
                }
                return "Tucked để tôn eo, untucked để casual hơn";
            }
        }
        // Source = dress tips
        if (srcGroup.contains("dress")) {
            if (pGroup.contains("jacket") || pGroup.contains("blazer")) {
                return "Mặc hờ jacket ngoài tạo dimension cho outfit";
            }
        }
        // Source = shoes tips
        if (srcGroup.contains("shoe") || srcGroup.contains("boot")) {
            return "Visible sock hoặc bare ankle tùy mood hôm đó";
        }
        // Accessory tips
        if (pGroup.contains("bag")) return "Chọn kích thước túi cân đối với tỷ lệ cơ thể";
        if (pGroup.contains("belt")) return "Belt to nhấn eo, belt nhỏ để tinh tế";
        if (pGroup.contains("glasses")) return "Eyewear là phụ kiện nhỏ nhưng thay đổi cả personality";
        return "";
    }

    /** Fallback reason when no tag signals available */
    private String generateFallbackReason(String srcGroup, String pGroup, String srcStyle,
                                          String pStyle, String colorHint) {
        String[][] fallbacks;

        if (pGroup.contains("bottom") || pGroup.contains("pant") || pGroup.contains("skirt")) {
            fallbacks = new String[][]{
                {"Phần dưới" + colorHint + " cân bằng tổng thể, tạo silhouette thanh thoát"},
                {"Chân váy/quần tôn lên phần trên, hoàn thiện cả bộ"},
                {"Bottom piece tạo nền tảng vững chắc cho outfit"},
            };
        } else if (pGroup.contains("shoe") || pGroup.contains("boot") || pGroup.contains("sneaker")) {
            fallbacks = new String[][]{
                {"Giày kết thúc outfit từ đầu đến chân, đừng bỏ qua"},
                {"Đôi giày" + colorHint + " là chữ ký cuối cùng của bộ trang phục"},
                {"Footwear game đỉnh — outfit mới thực sự hoàn chỉnh"},
            };
        } else if (pGroup.contains("bag") || pGroup.contains("purse")) {
            fallbacks = new String[][]{
                {"Túi" + colorHint + " vừa là phụ kiện vừa là statement piece"},
                {"Bag phù hợp nâng tổng thể outfit lên một tầm mới"},
                {"Carry piece" + colorHint + " tạo focal point thu hút ánh nhìn"},
            };
        } else if (pGroup.contains("outerwear") || pGroup.contains("jacket")) {
            fallbacks = new String[][]{
                {"Layer outerwear tạo depth và texture cho outfit"},
                {"Jacket/coat là \"hero piece\" kể câu chuyện phong cách"},
            };
        } else {
            fallbacks = new String[][]{
                {"Phụ kiện" + colorHint + " điểm nhấn nhẹ nhàng nhưng đủ sức thu hút"},
                {"Accessory nhỏ nhưng tạo nên sự khác biệt lớn trong tổng thể look"},
                {"Finishing touch" + colorHint + " — chi tiết tạo nên phong cách"},
            };
        }
        return fallbacks[new Random().nextInt(fallbacks.length)][0];
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPER TYPES
    // ══════════════════════════════════════════════════════════════════════════

    private static class ScoredProduct {
        Product product; int score; String reasonTitle, reasonDetail;
        ScoredProduct(Product p, int s, String rt, String rd) {
            product=p; score=s; reasonTitle=rt; reasonDetail=rd;
        }
    }

    private static class StyleScore { int score; String reason;
        StyleScore(int s, String r) { score=s; reason=r; } }

    private static class ColorScore { int score; String reason;
        ColorScore(int s, String r) { score=s; reason=r; } }

    private static class TypeScore  { int score; String reason;
        TypeScore(int s, String r)  { score=s; reason=r; } }

    // ══════════════════════════════════════════════════════════════════════════
    //  UTILITY
    // ══════════════════════════════════════════════════════════════════════════

    private boolean isSame(Product p) {
        if (p.getProductId() != null && mainProduct.getProductId() != null)
            return p.getProductId().equals(mainProduct.getProductId());
        return p.getName() != null && p.getName().equalsIgnoreCase(mainProduct.getName());
    }

    private boolean isSameProduct(Product a, Product b) {
        if (a.getProductId() != null && b.getProductId() != null)
            return a.getProductId().equals(b.getProductId());
        return a.getName() != null && a.getName().equalsIgnoreCase(b.getName());
    }

    private static String safe(String s) { return s != null ? s.trim().toLowerCase(Locale.ROOT) : ""; }

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void loadImage(ImageView iv, Product p) {
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            Glide.with(this).load(p.getImageUrl())
                .placeholder(R.color.colorCotton).error(R.color.colorCotton).into(iv);
        } else if (p.getImageResId() != 0) {
            iv.setImageResource(p.getImageResId());
        } else {
            iv.setImageResource(R.drawable.model1);
        }
    }

    // ─── Listeners ───────────────────────────────────────────────────────────
    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            finish();
        });
        if (btnAddAllToCart != null) btnAddAllToCart.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            addEntireOutfitToCart();
        });
    }

    private void addEntireOutfitToCart() {
        CartManager cm = CartManager.getInstance(this);

        applyDefaultsAndAdd(cm, mainProduct);
        for (Product p : recommendedList) applyDefaultsAndAdd(cm, p);

        ToastUtils.showCustomToast(this, getString(R.string.recommend_added_all_toast));
    }

    private void applyDefaultsAndAdd(CartManager cm, Product p) {
        if (p.getSelectedSize() == null) p.setSelectedSize("M");
        if (p.getSelectedColor() == null) {
            List<String> colors = p.getTagColor();
            p.setSelectedColor((colors != null && !colors.isEmpty()) ? cap(colors.get(0)) : "Black");
        }
        cm.addProduct(p, 1);
    }
}
