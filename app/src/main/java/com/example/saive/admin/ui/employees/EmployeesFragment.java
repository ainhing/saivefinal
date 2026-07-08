package com.example.saive.admin.ui.employees;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.saive.R;
import com.example.saive.admin.connectors.FirebaseConnector;
import com.example.saive.admin.data.model.AdminEmployee;
import com.example.saive.databinding.AdminFragmentEmployeesBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class EmployeesFragment extends Fragment {
    private AdminFragmentEmployeesBinding binding;
    private EmployeeAdapter adapter;
    private List<AdminEmployee> allEmployees = new ArrayList<>();
    private List<AdminEmployee> filteredEmployees = new ArrayList<>();
    private DatabaseReference dbRef;
    private ValueEventListener valueEventListener;
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminFragmentEmployeesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbRef = FirebaseConnector.getDatabase().getReference("Employees");

        setupRecyclerView();
        setupSearch();
        setupListeners();
        
        binding.swipeRefresh.setOnRefreshListener(this::loadEmployees);
        loadEmployees();
    }

    private void setupRecyclerView() {
        adapter = new EmployeeAdapter(new EmployeeAdapter.OnEmployeeClickListener() {
            @Override
            public void onEmployeeClick(AdminEmployee employee) {
                showEmployeeDialog(employee);
            }

            @Override
            public void onEmployeeActiveToggle(AdminEmployee employee, boolean isActive) {
                dbRef.child(employee.getEmployeeId()).child("IsActive").setValue(isActive)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), getString(R.string.admin_employee_toast_status_success), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), getString(R.string.admin_employee_toast_error) + e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadEmployees(); // Reload list to revert visual change
                        });
            }
        });
        binding.rvEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEmployees.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                filterEmployees();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupListeners() {
        binding.fabAddEmployee.setOnClickListener(v -> showEmployeeDialog(null));
    }

    private void loadEmployees() {
        binding.swipeRefresh.setRefreshing(true);
        if (valueEventListener != null) {
            dbRef.removeEventListener(valueEventListener);
        }

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEmployees.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    AdminEmployee emp = child.getValue(AdminEmployee.class);
                    if (emp != null) {
                        emp.setEmployeeId(child.getKey());
                        allEmployees.add(emp);
                    }
                }
                filterEmployees();
                if (binding != null) {
                    binding.swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), getString(R.string.admin_employee_toast_load_error) + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                if (binding != null) {
                    binding.swipeRefresh.setRefreshing(false);
                }
            }
        };

        dbRef.addValueEventListener(valueEventListener);
    }

    private void filterEmployees() {
        filteredEmployees.clear();
        for (AdminEmployee emp : allEmployees) {
            boolean matches = true;
            if (!currentSearchQuery.isEmpty()) {
                String name = emp.getFullName() != null ? emp.getFullName().toLowerCase() : "";
                String dept = emp.getDepartment() != null ? emp.getDepartment().toLowerCase() : "";
                String pos = emp.getPosition() != null ? emp.getPosition().toLowerCase() : "";
                matches = name.contains(currentSearchQuery) || dept.contains(currentSearchQuery) || pos.contains(currentSearchQuery);
            }
            if (matches) {
                filteredEmployees.add(emp);
            }
        }
        adapter.setEmployees(filteredEmployees);
    }

    private void showEmployeeDialog(@Nullable AdminEmployee employee) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.admin_dialog_employee, null);

        android.widget.TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        com.google.android.material.textfield.TextInputEditText etFullName = dialogView.findViewById(R.id.etFullName);
        com.google.android.material.textfield.TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        com.google.android.material.textfield.TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        com.google.android.material.textfield.TextInputEditText etDepartment = dialogView.findViewById(R.id.etDepartment);
        com.google.android.material.textfield.TextInputEditText etPosition = dialogView.findViewById(R.id.etPosition);
        com.google.android.material.switchmaterial.SwitchMaterial switchActive = dialogView.findViewById(R.id.switchActiveDetail);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        com.google.android.material.button.MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

        if (employee != null) {
            tvTitle.setText(getString(R.string.admin_employee_edit_title));
            etFullName.setText(employee.getFullName());
            etEmail.setText(employee.getEmail());
            etPhone.setText(employee.getPhone());
            etDepartment.setText(employee.getDepartment());
            etPosition.setText(employee.getPosition());
            switchActive.setChecked(employee.isActive());
        } else {
            tvTitle.setText(getString(R.string.admin_employee_add_title));
            switchActive.setChecked(true);
        }

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String dept = etDepartment.getText().toString().trim();
            String pos = etPosition.getText().toString().trim();
            boolean active = switchActive.isChecked();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || dept.isEmpty() || pos.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.admin_employee_toast_fields_required), Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("FullName", name);
            data.put("Email", email);
            data.put("Phone", phone);
            data.put("Department", dept);
            data.put("Position", pos);
            data.put("IsActive", active);

            if (employee != null) {
                // Edit mode
                dbRef.child(employee.getEmployeeId()).updateChildren(data)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), getString(R.string.admin_employee_toast_update_success), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), getString(R.string.admin_employee_toast_error) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Add mode: generate next ID EMPxxx
                int maxIdx = 0;
                for (AdminEmployee emp : allEmployees) {
                    if (emp.getEmployeeId() != null && emp.getEmployeeId().startsWith("EMP")) {
                        try {
                            int idx = Integer.parseInt(emp.getEmployeeId().substring(3));
                            if (idx > maxIdx) {
                                maxIdx = idx;
                            }
                        } catch (Exception e) {}
                    }
                }
                String nextId = String.format("EMP%03d", maxIdx + 1);
                data.put("CreatedAt", getISO8601Timestamp());

                dbRef.child(nextId).setValue(data)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), getString(R.string.admin_employee_toast_add_success), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), getString(R.string.admin_employee_toast_error) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private String getISO8601Timestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (valueEventListener != null) {
            dbRef.removeEventListener(valueEventListener);
        }
        binding = null;
    }
}
