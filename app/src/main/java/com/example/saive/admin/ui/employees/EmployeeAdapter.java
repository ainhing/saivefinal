package com.example.saive.admin.ui.employees;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.admin.data.model.AdminEmployee;
import com.example.saive.databinding.AdminItemEmployeeBinding;
import java.util.ArrayList;
import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {
    private List<AdminEmployee> employees = new ArrayList<>();
    private final OnEmployeeClickListener listener;

    public interface OnEmployeeClickListener {
        void onEmployeeClick(AdminEmployee employee);
        void onEmployeeActiveToggle(AdminEmployee employee, boolean isActive);
    }

    public EmployeeAdapter(OnEmployeeClickListener listener) {
        this.listener = listener;
    }

    public void setEmployees(List<AdminEmployee> newEmployees) {
        this.employees = newEmployees != null ? newEmployees : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminItemEmployeeBinding binding = AdminItemEmployeeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EmployeeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        holder.bind(employees.get(position));
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private final AdminItemEmployeeBinding binding;

        public EmployeeViewHolder(AdminItemEmployeeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AdminEmployee employee) {
            binding.tvEmployeeName.setText(employee.getFullName() != null ? employee.getFullName() : "—");
            
            String dept = employee.getDepartment() != null ? employee.getDepartment() : "—";
            String pos = employee.getPosition() != null ? employee.getPosition() : "—";
            binding.tvDepartmentPosition.setText(dept + " - " + pos);
            
            binding.tvEmployeePhone.setText(employee.getPhone() != null && !employee.getPhone().isEmpty() ? employee.getPhone() : "—");

            // Setup direct toggle switch
            binding.switchActive.setOnCheckedChangeListener(null);
            binding.switchActive.setChecked(employee.isActive());
            binding.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onEmployeeActiveToggle(employee, isChecked);
            });

            itemView.setOnClickListener(v -> listener.onEmployeeClick(employee));
        }
    }
}
