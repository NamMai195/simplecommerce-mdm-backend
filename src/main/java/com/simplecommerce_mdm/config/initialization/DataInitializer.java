package com.simplecommerce_mdm.config.initialization;

import com.simplecommerce_mdm.category.model.Category;
import com.simplecommerce_mdm.category.repository.CategoryRepository;
import com.simplecommerce_mdm.product.model.Shop;
import com.simplecommerce_mdm.product.repository.ShopRepository;
import com.simplecommerce_mdm.user.model.Role;
import com.simplecommerce_mdm.user.model.User;
import com.simplecommerce_mdm.user.repository.RoleRepository;
import com.simplecommerce_mdm.user.repository.UserRepository;
import com.simplecommerce_mdm.order.model.PaymentMethod;
import com.simplecommerce_mdm.order.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Data Initializer: Starting initialization...");

        // 1. Create default roles
        createDefaultRoles();

        // 2. Create sample users
        createSampleUsers();

        // 3. Create sample categories
        createSampleCategories();

        // 4. Create sample shop for seller
        createSampleShop();

        // 5. Create payment methods
        createPaymentMethods();

        log.info("✅ Data Initializer: Finished initialization!");
    }

    private void createDefaultRoles() {
        log.info("📝 Checking for default roles...");

        List<String> defaultRoles = Arrays.asList("ADMIN", "USER", "SELLER");

        for (String roleName : defaultRoles) {
            if (!roleRepository.existsByRoleName(roleName)) {
                Role newRole = Role.builder()
                        .roleName(roleName)
                        .description("Default " + roleName.toLowerCase() + " role")
                        .build();
                roleRepository.save(newRole);
                log.info("✅ Created default role: {}", roleName);
            } else {
                log.info("⏭️ Role '{}' already exists. Skipping.", roleName);
            }
        }
    }

    private void createSampleUsers() {
        log.info("👥 Creating sample users...");

        // Create Admin user
        createUserIfNotExists(
                "admin@simplecommerce.com",
                "Admin",
                "User",
                "admin123",
                "ADMIN"
        );

        // Create Seller user
        createUserIfNotExists(
                "seller@simplecommerce.com",
                "John",
                "Seller",
                "seller123",
                "SELLER"
        );

        // Create Regular user
        createUserIfNotExists(
                "user@simplecommerce.com",
                "Jane",
                "Customer",
                "user123",
                "USER"
        );
    }

    private void createUserIfNotExists(String email, String firstName, String lastName, String password, String roleName) {
        if (userRepository.findByEmail(email).isEmpty()) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found"));

            User user = User.builder()
                    .uuid(UUID.randomUUID())
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .passwordHash(passwordEncoder.encode(password))
                    .isActive(true)
                    .emailVerifiedAt(LocalDateTime.now())
                    .roles(Set.of(role))
                    .build();

            userRepository.save(user);
            log.info("✅ Created sample user: {} ({})", email, roleName);
        } else {
            log.info("⏭️ User {} already exists. Skipping.", email);
        }
    }

    private void createSampleCategories() {
        log.info("📂 Creating sample categories...");

        // Main categories
        createCategoryIfNotExists("Electronics", "Electronic devices and gadgets", null);
        createCategoryIfNotExists("Fashion", "Clothing and accessories", null);
        createCategoryIfNotExists("Home & Garden", "Home decoration and garden supplies", null);
        createCategoryIfNotExists("Sports", "Sports equipment and accessories", null);

        // Sub-categories for Electronics
        Category electronics = categoryRepository.findBySlug("electronics").orElse(null);
        if (electronics != null) {
            createCategoryIfNotExists("Smartphones", "Mobile phones and accessories", electronics);
            createCategoryIfNotExists("Laptops", "Laptops and computers", electronics);
            createCategoryIfNotExists("Audio", "Headphones and speakers", electronics);
        }

        // Sub-categories for Fashion
        Category fashion = categoryRepository.findBySlug("fashion").orElse(null);
        if (fashion != null) {
            createCategoryIfNotExists("Men's Clothing", "Clothing for men", fashion);
            createCategoryIfNotExists("Women's Clothing", "Clothing for women", fashion);
            createCategoryIfNotExists("Shoes", "Footwear for all", fashion);
        }
    }

    private void createCategoryIfNotExists(String name, String description, Category parent) {
        String slug = generateSlug(name);
        if (categoryRepository.findBySlug(slug).isEmpty()) {
            Category category = Category.builder()
                    .name(name)
                    .slug(slug)
                    .description(description)
                    .parent(parent)
                    .isActive(true)
                    .sortOrder(0)
                    .build();

            categoryRepository.save(category);
            log.info("✅ Created sample category: {} (slug: {})", name, slug);
        } else {
            log.info("⏭️ Category {} already exists. Skipping.", name);
        }
    }

    private void createSampleShop() {
        log.info("🏪 Creating sample shop...");

        Optional<User> sellerUser = userRepository.findByEmail("seller@simplecommerce.com");
        if (sellerUser.isPresent() && shopRepository.findByUser(sellerUser.get()).isEmpty()) {
            Shop shop = Shop.builder()
                    .user(sellerUser.get())
                    .name("John's Electronics Store")
                    .slug(generateSlug("John's Electronics Store"))
                    .description("Quality electronics and gadgets for everyone. We offer the best prices and fast delivery.")
                    .contactEmail("contact@johnselectronics.com")
                    .contactPhone("+84901234567")
                    .isActive(true)
                    .approvedAt(java.time.OffsetDateTime.now())
                    .build();

            shopRepository.save(shop);
            log.info("✅ Created sample shop: {} for seller: {}", shop.getName(), sellerUser.get().getEmail());
        } else {
            log.info("⏭️ Sample shop already exists or seller not found. Skipping.");
        }
    }

    private void createPaymentMethods() {
        log.info("💳 Creating payment methods...");

        // Create COD payment method
        if (paymentMethodRepository.findByCode("COD").isEmpty()) {
            PaymentMethod cod = PaymentMethod.builder()
                    .name("Cash on Delivery")
                    .code("COD")
                    .description("Pay with cash when you receive your order")
                    .isActive(true)
                    .sortOrder(1)
                    .build();
            paymentMethodRepository.save(cod);
            log.info("✅ Created payment method: COD (Cash on Delivery)");
        } else {
            log.info("⏭️ Payment method COD already exists. Skipping.");
        }

        // Create Bank Transfer payment method (for future use)
        if (paymentMethodRepository.findByCode("BANK_TRANSFER").isEmpty()) {
            PaymentMethod bankTransfer = PaymentMethod.builder()
                    .name("Bank Transfer")
                    .code("BANK_TRANSFER")
                    .description("Transfer money to our bank account")
                    .isActive(false) // Disabled for now
                    .sortOrder(2)
                    .build();
            paymentMethodRepository.save(bankTransfer);
            log.info("✅ Created payment method: BANK_TRANSFER (disabled)");
        } else {
            log.info("⏭️ Payment method BANK_TRANSFER already exists. Skipping.");
        }
    }

    private String generateSlug(String input) {
        if (input == null) {
            return "";
        }
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
} 