package com.example.lostfound.config;

import com.example.lostfound.entity.*;
import com.example.lostfound.repository.ItemRepository;
import com.example.lostfound.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ItemRepository itemRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Initialize default administrator if not present
        User admin = userRepository.findByEmail("admin@findit.edu").orElseGet(() -> {
            User newAdmin = new User(
                    "Campus Administrator",
                    "admin@findit.edu",
                    passwordEncoder.encode("Admin@123"),
                    "+1-555-0100",
                    Role.ADMIN
            );
            logger.info("Seeding default administrator account: admin@findit.edu");
            return userRepository.save(newAdmin);
        });

        // Initialize default student user if not present
        User student = userRepository.findByEmail("student@findit.edu").orElseGet(() -> {
            User newStudent = new User(
                    "Alex Johnson",
                    "student@findit.edu",
                    passwordEncoder.encode("Student@123"),
                    "+1-555-0199",
                    Role.USER
            );
            logger.info("Seeding default student account: student@findit.edu");
            return userRepository.save(newStudent);
        });

        // Seed sample items if database has no items
        if (itemRepository.count() == 0) {
            logger.info("Seeding initial lost and found items...");

            Item item1 = new Item(
                    "TI-84 Plus CE Graphing Calculator",
                    "Found left on desk near projector screen. Has a blue sticker on back case.",
                    ItemCategory.ELECTRONICS,
                    ReportType.FOUND,
                    "Science Hall Room 204",
                    LocalDate.now().minusDays(1),
                    admin
            );

            Item item2 = new Item(
                    "Hydro Flask 32oz (Cobalt Blue)",
                    "Lost my water bottle with several campus club stickers and slight dent on base.",
                    ItemCategory.OTHER,
                    ReportType.LOST,
                    "Campus Recreation Gym Locker Room",
                    LocalDate.now().minusDays(2),
                    student
            );

            Item item3 = new Item(
                    "Brown Leather Wallet with Student ID",
                    "Found brown trifold leather wallet. Contained student ID card and bus pass.",
                    ItemCategory.ACCESSORIES,
                    ReportType.FOUND,
                    "Main Library 2nd Floor Study Area",
                    LocalDate.now().minusDays(3),
                    admin
            );

            Item item4 = new Item(
                    "Apple AirPods Pro (2nd Gen)",
                    "Lost AirPods in a matte black protective case with a small carabiner clip.",
                    ItemCategory.ELECTRONICS,
                    ReportType.LOST,
                    "Student Union Cafeteria Booths",
                    LocalDate.now().minusDays(1),
                    student
            );

            itemRepository.save(item1);
            itemRepository.save(item2);
            itemRepository.save(item3);
            itemRepository.save(item4);
            logger.info("Sample items seeded successfully.");
        }
    }
}
