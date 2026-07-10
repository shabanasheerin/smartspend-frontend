package com.smartspend.config;

import com.smartspend.entity.Category;
import com.smartspend.entity.Role;
import com.smartspend.repository.CategoryRepository;
import com.smartspend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedCategories();
    }

    private void seedRoles() {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                return roleRepository.save(role);
            });
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<String> expenseCategories = List.of(
                "Food", "Rent", "Shopping", "Entertainment", "Medical",
                "Education", "Travel", "Transport", "Bills", "Investment", "Others"
        );

        for (String name : expenseCategories) {
            categoryRepository.save(Category.builder()
                    .name(name)
                    .type(Category.CategoryType.EXPENSE)
                    .systemDefined(true)
                    .build());
        }
    }
}
