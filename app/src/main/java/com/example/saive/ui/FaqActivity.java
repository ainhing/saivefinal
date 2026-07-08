package com.example.saive.ui;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.adapters.FaqAdapter;
import com.example.saive.models.Faqitem;

import java.util.ArrayList;
import java.util.List;

public class FaqActivity extends AppCompatActivity {

    private RecyclerView rvFaq;
    private FaqAdapter adapter;
    private List<Faqitem> faqList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_faq);

        setupWindowInsets();
        initData();
        setupRecyclerView();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void initData() {
        int[] qResIds = {
            R.string.faq_q1, R.string.faq_q2, R.string.faq_q3, R.string.faq_q4, R.string.faq_q5,
            R.string.faq_q6, R.string.faq_q7, R.string.faq_q8, R.string.faq_q9, R.string.faq_q10
        };
        int[] aResIds = {
            R.string.faq_a1, R.string.faq_a2, R.string.faq_a3, R.string.faq_a4, R.string.faq_a5,
            R.string.faq_a6, R.string.faq_a7, R.string.faq_a8, R.string.faq_a9, R.string.faq_a10
        };

        for (int i = 0; i < qResIds.length; i++) {
            faqList.add(new Faqitem(getString(qResIds[i]), getString(aResIds[i])));
        }
    }

    private void setupRecyclerView() {
        rvFaq = findViewById(R.id.rvFaq);
        rvFaq.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FaqAdapter(faqList);
        rvFaq.setAdapter(adapter);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}