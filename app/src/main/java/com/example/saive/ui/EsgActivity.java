package com.example.saive.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.saive.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * EsgActivity — ESG Research Hub
 * Yêu cầu 2 từ thầy Trần Duy Thanh:
 * - Tra cứu điểm số ESG của các công ty toàn cầu
 * - So sánh điểm số ESG của nhiều công ty
 * - Mô phỏng tác động của ESG đến hiệu quả tài chính
 *
 * Dataset: company_esg_financial_dataset.csv (1000 công ty, 2010–2020)
 * Columns: CompanyID, CompanyName, Industry, Region, Year,
 *           Revenue, ProfitMargin, MarketCap, GrowthRate,
 *           ESG_Overall, ESG_Environmental, ESG_Social, ESG_Governance,
 *           CarbonEmissions, WaterUsage, EnergyConsumption
 */
public class EsgActivity extends AppCompatActivity {

    // ── Dataset ──────────────────────────────────────────────────────
    private List<EsgRecord> allRecords = new ArrayList<>();
    private List<String> allCompanyNames = new ArrayList<>();
    private boolean dataLoaded = false;

    // ── Tabs ─────────────────────────────────────────────────────────
    private View tabSearch, tabCompare, tabSimulate;
    private View panelSearch, panelCompare, panelSimulate;
    private Button btnTabSearch, btnTabCompare, btnTabSimulate;

    // ── Tab 1: Search ──────────────────────────────────────────────
    private EditText etSearchCompany;
    private Spinner spinnerYear;
    private LinearLayout searchResultContainer;
    private ProgressBar pbSearch;

    // ── Tab 2: Compare ────────────────────────────────────────────
    private Spinner spinnerCmp1, spinnerCmp2, spinnerCmpYear;
    private LinearLayout compareResultContainer;

    // ── Tab 3: Simulate ───────────────────────────────────────────
    private Spinner spinnerSimIndustry, spinnerSimRegion;
    private EditText etSimEsgOverall;
    private LinearLayout simulateResultContainer;

    private static final String LANG_PREFS = "language_prefs";
    private static final String LANG_KEY = "selected_language";

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(LANG_PREFS, MODE_PRIVATE);
        String lang = prefs.getString(LANG_KEY, "en");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = newBase.getResources().getConfiguration();
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esg);

        initViews();
        setupTabs();
        loadDataAsync();

        // Back button
        View btnBack = findViewById(R.id.btnEsgBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                onBackPressed();
            });
        }
    }

    // ── Views ─────────────────────────────────────────────────────────
    private void initViews() {
        btnTabSearch = findViewById(R.id.btnTabSearch);
        btnTabCompare = findViewById(R.id.btnTabCompare);
        btnTabSimulate = findViewById(R.id.btnTabSimulate);

        panelSearch = findViewById(R.id.panelSearch);
        panelCompare = findViewById(R.id.panelCompare);
        panelSimulate = findViewById(R.id.panelSimulate);

        etSearchCompany = findViewById(R.id.etSearchCompany);
        spinnerYear = findViewById(R.id.spinnerYear);
        searchResultContainer = findViewById(R.id.searchResultContainer);
        pbSearch = findViewById(R.id.pbSearch);

        spinnerCmp1 = findViewById(R.id.spinnerCmp1);
        spinnerCmp2 = findViewById(R.id.spinnerCmp2);
        spinnerCmpYear = findViewById(R.id.spinnerCmpYear);
        compareResultContainer = findViewById(R.id.compareResultContainer);

        spinnerSimIndustry = findViewById(R.id.spinnerSimIndustry);
        spinnerSimRegion = findViewById(R.id.spinnerSimRegion);
        etSimEsgOverall = findViewById(R.id.etSimEsgOverall);
        simulateResultContainer = findViewById(R.id.simulateResultContainer);
    }

    // ── Tabs ─────────────────────────────────────────────────────────
    private void setupTabs() {
        selectTab(0);
        btnTabSearch.setOnClickListener(v -> selectTab(0));
        btnTabCompare.setOnClickListener(v -> selectTab(1));
        btnTabSimulate.setOnClickListener(v -> selectTab(2));

        Button btnDoSearch = findViewById(R.id.btnDoSearch);
        if (btnDoSearch != null) btnDoSearch.setOnClickListener(v -> doSearch());

        Button btnDoCompare = findViewById(R.id.btnDoCompare);
        if (btnDoCompare != null) btnDoCompare.setOnClickListener(v -> doCompare());

        Button btnDoSimulate = findViewById(R.id.btnDoSimulate);
        if (btnDoSimulate != null) btnDoSimulate.setOnClickListener(v -> doSimulate());
    }

    private void selectTab(int tab) {
        panelSearch.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        panelCompare.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);
        panelSimulate.setVisibility(tab == 2 ? View.VISIBLE : View.GONE);

        int active = ContextCompat.getColor(this, R.color.colorOnMaroon);
        int inactive = ContextCompat.getColor(this, R.color.colorOnMaroonSecondary);

        btnTabSearch.setTextColor(tab == 0 ? active : inactive);
        btnTabCompare.setTextColor(tab == 1 ? active : inactive);
        btnTabSimulate.setTextColor(tab == 2 ? active : inactive);
    }

    // ── Data Loading ─────────────────────────────────────────────────
    @SuppressLint("StaticFieldLeak")
    private void loadDataAsync() {
        if (pbSearch != null) pbSearch.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, List<EsgRecord>>() {
            @Override
            protected List<EsgRecord> doInBackground(Void... voids) {
                return loadCsv();
            }

            @Override
            protected void onPostExecute(List<EsgRecord> records) {
                allRecords = records;
                dataLoaded = true;
                if (pbSearch != null) pbSearch.setVisibility(View.GONE);
                setupSpinnersAfterLoad();
                showDefaultSearchHint();
            }
        }.execute();
    }

    private List<EsgRecord> loadCsv() {
        List<EsgRecord> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(getAssets().open("company_esg_financial_dataset.csv")));
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",", -1);
                if (cols.length < 16) continue;
                try {
                    EsgRecord r = new EsgRecord();
                    r.companyId = parseIntSafe(cols[0]);
                    r.companyName = cols[1].trim();
                    r.industry = cols[2].trim();
                    r.region = cols[3].trim();
                    r.year = parseIntSafe(cols[4]);
                    r.revenue = parseDoubleSafe(cols[5]);
                    r.profitMargin = parseDoubleSafe(cols[6]);
                    r.marketCap = parseDoubleSafe(cols[7]);
                    r.growthRate = parseDoubleSafe(cols[8]);
                    r.esgOverall = parseDoubleSafe(cols[9]);
                    r.esgEnvironmental = parseDoubleSafe(cols[10]);
                    r.esgSocial = parseDoubleSafe(cols[11]);
                    r.esgGovernance = parseDoubleSafe(cols[12]);
                    r.carbonEmissions = parseDoubleSafe(cols[13]);
                    r.waterUsage = parseDoubleSafe(cols[14]);
                    r.energyConsumption = parseDoubleSafe(cols[15]);
                    list.add(r);
                } catch (Exception ignored) {}
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void setupSpinnersAfterLoad() {
        // Collect unique names, years, industries, regions
        Map<String, Boolean> nameMap = new LinkedHashMap<>();
        List<String> years = new ArrayList<>();
        List<String> industries = new ArrayList<>();
        List<String> regions = new ArrayList<>();

        for (EsgRecord r : allRecords) {
            nameMap.put(r.companyName, true);
            if (!years.contains(String.valueOf(r.year))) years.add(String.valueOf(r.year));
            if (!industries.contains(r.industry)) industries.add(r.industry);
            if (!regions.contains(r.region)) regions.add(r.region);
        }

        allCompanyNames = new ArrayList<>(nameMap.keySet());
        Collections.sort(allCompanyNames);
        Collections.sort(years);
        Collections.sort(industries);
        Collections.sort(regions);

        // Year spinners
        List<String> yearWithAll = new ArrayList<>();
        yearWithAll.add(getString(R.string.esg_all_years));
        yearWithAll.addAll(years);
        setSpinnerAdapter(spinnerYear, yearWithAll);
        setSpinnerAdapter(spinnerCmpYear, years.isEmpty() ? yearWithAll : years);

        // Compare company spinners
        setSpinnerAdapter(spinnerCmp1, allCompanyNames);
        setSpinnerAdapter(spinnerCmp2, allCompanyNames);
        if (allCompanyNames.size() > 1) spinnerCmp2.setSelection(1);

        // Simulate spinners
        List<String> indWithAll = new ArrayList<>();
        indWithAll.add(getString(R.string.esg_all_industries));
        indWithAll.addAll(industries);
        setSpinnerAdapter(spinnerSimIndustry, indWithAll);

        List<String> regWithAll = new ArrayList<>();
        regWithAll.add(getString(R.string.esg_all_regions));
        regWithAll.addAll(regions);
        setSpinnerAdapter(spinnerSimRegion, regWithAll);
    }

    private void setSpinnerAdapter(Spinner spinner, List<String> items) {
        if (spinner == null || items == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void showDefaultSearchHint() {
        if (searchResultContainer == null) return;
        searchResultContainer.removeAllViews();
        addInfoCard(searchResultContainer,
                getString(R.string.esg_guide_title),
                getString(R.string.esg_guide_desc),
                "#2E7D32");
    }

    // ── Tab 1: Tra cứu ───────────────────────────────────────────────
    private void doSearch() {
        if (!dataLoaded) { Toast.makeText(this, getString(R.string.esg_loading_data), Toast.LENGTH_SHORT).show(); return; }
        String keyword = etSearchCompany.getText().toString().trim().toLowerCase();
        if (keyword.isEmpty()) { Toast.makeText(this, getString(R.string.esg_enter_company_name), Toast.LENGTH_SHORT).show(); return; }

        String selectedYear = spinnerYear != null ? (String) spinnerYear.getSelectedItem() : getString(R.string.esg_all_years);
        boolean filterYear = selectedYear != null && !selectedYear.equals(getString(R.string.esg_all_years));
        int yearFilter = filterYear ? parseIntSafe(selectedYear) : 0;

        List<EsgRecord> results = new ArrayList<>();
        for (EsgRecord r : allRecords) {
            if (r.companyName.toLowerCase().contains(keyword)) {
                if (!filterYear || r.year == yearFilter) results.add(r);
            }
        }

        searchResultContainer.removeAllViews();

        if (results.isEmpty()) {
            addInfoCard(searchResultContainer, getString(R.string.esg_not_found_title),
                    getString(R.string.esg_not_found_desc, keyword), "#C62828");
            return;
        }

        // Group by company name then year
        Map<String, List<EsgRecord>> grouped = new LinkedHashMap<>();
        for (EsgRecord r : results) {
            grouped.computeIfAbsent(r.companyName, k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<String, List<EsgRecord>> entry : grouped.entrySet()) {
            List<EsgRecord> compRecords = entry.getValue();
            EsgRecord latest = compRecords.get(compRecords.size() - 1);

            // Calculate averages
            double avgEsg = 0, avgProfit = 0, avgGrowth = 0;
            for (EsgRecord r : compRecords) { avgEsg += r.esgOverall; avgProfit += r.profitMargin; avgGrowth += r.growthRate; }
            avgEsg /= compRecords.size(); avgProfit /= compRecords.size(); avgGrowth /= compRecords.size();

            addCompanyCard(searchResultContainer, latest, compRecords, avgEsg, avgProfit, avgGrowth);
        }
    }

    // ── Tab 2: So sánh ───────────────────────────────────────────────
    private void doCompare() {
        if (!dataLoaded) return;
        String name1 = spinnerCmp1 != null ? (String) spinnerCmp1.getSelectedItem() : null;
        String name2 = spinnerCmp2 != null ? (String) spinnerCmp2.getSelectedItem() : null;
        String yearStr = spinnerCmpYear != null ? (String) spinnerCmpYear.getSelectedItem() : null;

        if (name1 == null || name2 == null || name1.equals(name2)) {
            Toast.makeText(this, getString(R.string.esg_select_different_companies), Toast.LENGTH_SHORT).show();
            return;
        }
        int yearNum = parseIntSafe(yearStr);

        EsgRecord r1 = findRecord(name1, yearNum);
        EsgRecord r2 = findRecord(name2, yearNum);

        compareResultContainer.removeAllViews();

        if (r1 == null || r2 == null) {
            addInfoCard(compareResultContainer, getString(R.string.esg_no_data_title),
                    getString(R.string.esg_no_data_year_desc, yearNum), "#C62828");
            return;
        }

        addCompareCard(compareResultContainer, r1, r2);
    }

    // ── Tab 3: Mô phỏng ──────────────────────────────────────────────
    private void doSimulate() {
        if (!dataLoaded) return;

        String esgStr = etSimEsgOverall != null ? etSimEsgOverall.getText().toString().trim() : "";

        if (esgStr.isEmpty()) { Toast.makeText(this, getString(R.string.esg_enter_esg_score), Toast.LENGTH_SHORT).show(); return; }

        double inputEsg = parseDoubleSafe(esgStr);
        if (inputEsg < 0 || inputEsg > 100) { Toast.makeText(this, getString(R.string.esg_invalid_esg_score), Toast.LENGTH_SHORT).show(); return; }

        String selIndustry = spinnerSimIndustry != null ? (String) spinnerSimIndustry.getSelectedItem() : getString(R.string.esg_all_industries);
        String selRegion = spinnerSimRegion != null ? (String) spinnerSimRegion.getSelectedItem() : getString(R.string.esg_all_regions);

        // Filter records
        List<EsgRecord> filtered = new ArrayList<>();
        for (EsgRecord r : allRecords) {
            boolean okInd = selIndustry == null || selIndustry.equals(getString(R.string.esg_all_industries)) || r.industry.equals(selIndustry);
            boolean okReg = selRegion == null || selRegion.equals(getString(R.string.esg_all_regions)) || r.region.equals(selRegion);
            if (okInd && okReg) filtered.add(r);
        }

        if (filtered.isEmpty()) {
            simulateResultContainer.removeAllViews();
            addInfoCard(simulateResultContainer, getString(R.string.esg_no_data_title), getString(R.string.esg_no_data_filter_desc), "#C62828");
            return;
        }

        // Regression-style prediction: use linear relationship ESG → ProfitMargin, MarketCap, GrowthRate
        double sumX = 0, sumY1 = 0, sumY2 = 0, sumY3 = 0, sumXX = 0, sumXY1 = 0, sumXY2 = 0, sumXY3 = 0;
        int n = filtered.size();
        for (EsgRecord r : filtered) {
            sumX += r.esgOverall;
            sumY1 += r.profitMargin;
            sumY2 += r.marketCap;
            sumY3 += r.growthRate;
            sumXX += r.esgOverall * r.esgOverall;
            sumXY1 += r.esgOverall * r.profitMargin;
            sumXY2 += r.esgOverall * r.marketCap;
            sumXY3 += r.esgOverall * r.growthRate;
        }

        double denom = n * sumXX - sumX * sumX;
        double b1 = (denom != 0) ? (n * sumXY1 - sumX * sumY1) / denom : 0;
        double a1 = (sumY1 - b1 * sumX) / n;

        double b2 = (denom != 0) ? (n * sumXY2 - sumX * sumY2) / denom : 0;
        double a2 = (sumY2 - b2 * sumX) / n;

        double b3 = (denom != 0) ? (n * sumXY3 - sumX * sumY3) / denom : 0;
        double a3 = (sumY3 - b3 * sumX) / n;

        double predProfitMargin = a1 + b1 * inputEsg;
        
        // Kế thừa thực nghiệm: Dùng hệ số trễ (+0.0130) từ mô hình Fixed Effects Lag 1 (Phần 5.3) của Notebook
        double meanEsg = sumX / n;
        double meanMarket = sumY2 / n;
        double deltaEsg = inputEsg - meanEsg;
        double predMarketCap = meanMarket * Math.exp(0.0130 * deltaEsg);
        
        double predGrowthRate = a3 + b3 * inputEsg;

        // Average ESG sub-scores for comparison bucket
        double esgLow = inputEsg - 10, esgHigh = inputEsg + 10;
        List<EsgRecord> bucket = new ArrayList<>();
        for (EsgRecord r : filtered) {
            if (r.esgOverall >= esgLow && r.esgOverall <= esgHigh) bucket.add(r);
        }
        double avgEnv = 0, avgSoc = 0, avgGov = 0;
        if (!bucket.isEmpty()) {
            for (EsgRecord r : bucket) { avgEnv += r.esgEnvironmental; avgSoc += r.esgSocial; avgGov += r.esgGovernance; }
            avgEnv /= bucket.size(); avgSoc /= bucket.size(); avgGov /= bucket.size();
        }

        // Correlation coefficient ESG_Overall vs ProfitMargin
        double meanX = sumX / n, meanY1v = sumY1 / n;
        double covXY1 = 0, varX = 0, varY1 = 0;
        for (EsgRecord r : filtered) {
            covXY1 += (r.esgOverall - meanX) * (r.profitMargin - meanY1v);
            varX += (r.esgOverall - meanX) * (r.esgOverall - meanX);
            varY1 += (r.profitMargin - meanY1v) * (r.profitMargin - meanY1v);
        }
        double corr = (varX > 0 && varY1 > 0) ? covXY1 / Math.sqrt(varX * varY1) : 0;

        simulateResultContainer.removeAllViews();
        addSimulateResultCard(simulateResultContainer,
                inputEsg, selIndustry, selRegion,
                predProfitMargin, predMarketCap, predGrowthRate,
                avgEnv, avgSoc, avgGov, corr, n, bucket.size(),
                b1);
    }

    // ── UI Builders ───────────────────────────────────────────────────
    private void addInfoCard(LinearLayout container, String title, String msg, String hexColor) {
        View card = LayoutInflater.from(this).inflate(R.layout.esg_card_info, container, false);
        TextView tvTitle = card.findViewById(R.id.esgCardTitle);
        TextView tvMsg = card.findViewById(R.id.esgCardMsg);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvMsg != null) tvMsg.setText(msg);
        container.addView(card);
    }

    private void addCompanyCard(LinearLayout container, EsgRecord latest, List<EsgRecord> history,
                                double avgEsg, double avgProfit, double avgGrowth) {
        View card = LayoutInflater.from(this).inflate(R.layout.esg_card_company, container, false);

        setTextSafe(card, R.id.esgCompanyName, latest.companyName);
        setTextSafe(card, R.id.esgIndustry, latest.industry + " · " + latest.region);
        setTextSafe(card, R.id.esgOverallScore, String.format(Locale.US, "%.1f", avgEsg));
        setTextSafe(card, R.id.esgEnvScore, String.format(Locale.US, "%.1f", latest.esgEnvironmental));
        setTextSafe(card, R.id.esgSocScore, String.format(Locale.US, "%.1f", latest.esgSocial));
        setTextSafe(card, R.id.esgGovScore, String.format(Locale.US, "%.1f", latest.esgGovernance));
        setTextSafe(card, R.id.esgProfitMargin, String.format(Locale.US, "%.2f%%", avgProfit));
        setTextSafe(card, R.id.esgGrowthRate, String.format(Locale.US, "%.2f%%", avgGrowth));
        setTextSafe(card, R.id.esgMarketCap, String.format(Locale.US, "%.0f M USD", latest.marketCap));
        setTextSafe(card, R.id.esgYearsRange, getString(R.string.esg_years_data_format, history.size(),
                history.get(0).year, history.get(history.size() - 1).year));

        String rating = getEsgRating(avgEsg);
        setTextSafe(card, R.id.esgRatingBadge, rating);

        container.addView(card);
    }

    private void addCompareCard(LinearLayout container, EsgRecord r1, EsgRecord r2) {
        View card = LayoutInflater.from(this).inflate(R.layout.esg_card_compare, container, false);

        setTextSafe(card, R.id.cmpName1, r1.companyName);
        setTextSafe(card, R.id.cmpName2, r2.companyName);
        setTextSafe(card, R.id.cmpYear, getString(R.string.esg_year_format, r1.year));

        setTextSafe(card, R.id.cmpEsg1, fmt1(r1.esgOverall));
        setTextSafe(card, R.id.cmpEsg2, fmt1(r2.esgOverall));
        setTextSafe(card, R.id.cmpEnv1, fmt1(r1.esgEnvironmental));
        setTextSafe(card, R.id.cmpEnv2, fmt1(r2.esgEnvironmental));
        setTextSafe(card, R.id.cmpSoc1, fmt1(r1.esgSocial));
        setTextSafe(card, R.id.cmpSoc2, fmt1(r2.esgSocial));
        setTextSafe(card, R.id.cmpGov1, fmt1(r1.esgGovernance));
        setTextSafe(card, R.id.cmpGov2, fmt1(r2.esgGovernance));
        setTextSafe(card, R.id.cmpProfit1, fmt2(r1.profitMargin) + "%");
        setTextSafe(card, R.id.cmpProfit2, fmt2(r2.profitMargin) + "%");
        setTextSafe(card, R.id.cmpMarket1, fmt0(r1.marketCap) + "M");
        setTextSafe(card, R.id.cmpMarket2, fmt0(r2.marketCap) + "M");
        setTextSafe(card, R.id.cmpGrowth1, fmt2(r1.growthRate) + "%");
        setTextSafe(card, R.id.cmpGrowth2, fmt2(r2.growthRate) + "%");

        String winner = r1.esgOverall >= r2.esgOverall ? r1.companyName : r2.companyName;
        setTextSafe(card, R.id.cmpWinner, getString(R.string.esg_winner_format, winner));

        container.addView(card);
    }

    private void addSimulateResultCard(LinearLayout container,
                                       double inputEsg, String industry, String region,
                                       double predProfit, double predMarket, double predGrowth,
                                       double avgEnv, double avgSoc, double avgGov,
                                       double corr, int totalN, int bucketN,
                                       double slope) {
        View card = LayoutInflater.from(this).inflate(R.layout.esg_card_simulate, container, false);

        setTextSafe(card, R.id.simInputEsg, fmt1(inputEsg) + " / 100");
        setTextSafe(card, R.id.simScope, industry + " · " + region + " (" + totalN + " " + getString(R.string.esg_sim_observations) + ")");
        setTextSafe(card, R.id.simPredProfit, fmt2(predProfit) + "%");
        setTextSafe(card, R.id.simPredMarket, fmt0(predMarket) + " M USD");
        setTextSafe(card, R.id.simPredGrowth, fmt2(predGrowth) + "%");
        setTextSafe(card, R.id.simAvgEnv, fmt1(avgEnv));
        setTextSafe(card, R.id.simAvgSoc, fmt1(avgSoc));
        setTextSafe(card, R.id.simAvgGov, fmt1(avgGov));

        String corrDetail;
        if (Math.abs(corr) >= 0.5) {
            corrDetail = corr > 0 ? getString(R.string.esg_sim_corr_significant_pos) : getString(R.string.esg_sim_corr_significant_neg);
        } else if (Math.abs(corr) >= 0.2) {
            corrDetail = corr > 0 ? getString(R.string.esg_sim_corr_weak_pos) : getString(R.string.esg_sim_corr_weak_neg);
        } else {
            corrDetail = getString(R.string.esg_sim_corr_insignificant);
        }
        String corrTxt = getString(R.string.esg_sim_correlation_format, corr, corrDetail);
        setTextSafe(card, R.id.simCorr, corrTxt);

        String slopeTxt = slope > 0
                ? String.format(Locale.US, "+%.3f%% %s", slope, getString(R.string.esg_sim_profit_per_esg))
                : String.format(Locale.US, "%.3f%% %s", slope, getString(R.string.esg_sim_profit_per_esg));
        setTextSafe(card, R.id.simSlope, slopeTxt);

        String insight;
        if (inputEsg >= 70) insight = getString(R.string.esg_sim_insight_high);
        else if (inputEsg >= 50) insight = getString(R.string.esg_sim_insight_mid);
        else insight = getString(R.string.esg_sim_insight_low);
        setTextSafe(card, R.id.simInsight, insight);

        container.addView(card);
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private EsgRecord findRecord(String name, int year) {
        for (EsgRecord r : allRecords) {
            if (r.companyName.equals(name) && r.year == year) return r;
        }
        return null;
    }

    private String getEsgRating(double score) {
        if (score >= 80) return "AAA";
        if (score >= 70) return "AA";
        if (score >= 60) return "A";
        if (score >= 50) return "BBB";
        if (score >= 40) return "BB";
        return "B";
    }

    private void setTextSafe(View root, int id, String text) {
        TextView tv = root.findViewById(id);
        if (tv != null && text != null) tv.setText(text);
    }

    private String fmt1(double v) { return String.format(Locale.US, "%.1f", v); }
    private String fmt2(double v) { return String.format(Locale.US, "%.2f", v); }
    private String fmt0(double v) { return String.format(Locale.US, "%.0f", v); }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
    private double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; }
    }

    // ── Data Model ───────────────────────────────────────────────────
    static class EsgRecord {
        int companyId, year;
        String companyName, industry, region;
        double revenue, profitMargin, marketCap, growthRate;
        double esgOverall, esgEnvironmental, esgSocial, esgGovernance;
        double carbonEmissions, waterUsage, energyConsumption;
    }
}
