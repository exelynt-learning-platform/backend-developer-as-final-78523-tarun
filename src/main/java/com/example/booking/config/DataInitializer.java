package com.example.booking.config;

import com.example.booking.model.Resource;
import com.example.booking.model.User;
import com.example.booking.model.User.Role;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    UserRepository userRepository;

    @Autowired
    ResourceRepository resourceRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create seed users if they don't exist
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setRoles(java.util.Set.of(Role.ADMIN));
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@example.com");
            user.setRoles(java.util.Set.of(Role.USER));
            userRepository.save(user);
        }

        // Create sample resources if they don't exist
        if (resourceRepository.count() == 0) {
            // Conference Room A
            Resource conferenceRoomA = new Resource();
            conferenceRoomA.setName("Conference Room A");
            conferenceRoomA.setDescription("Large conference room with projector and whiteboard");
            conferenceRoomA.setPrice(new BigDecimal("50.00"));
            conferenceRoomA.setIsAvailable(true);
            resourceRepository.save(conferenceRoomA);

            // Conference Room B
            Resource conferenceRoomB = new Resource();
            conferenceRoomB.setName("Conference Room B");
            conferenceRoomB.setDescription("Medium conference room with video conferencing");
            conferenceRoomB.setPrice(new BigDecimal("75.00"));
            conferenceRoomB.setIsAvailable(true);
            resourceRepository.save(conferenceRoomB);

            // Projector
            Resource projector = new Resource();
            projector.setName("4K Projector");
            projector.setDescription("High-resolution projector for presentations");
            projector.setPrice(new BigDecimal("25.00"));
            projector.setIsAvailable(true);
            resourceRepository.save(projector);

            // Meeting Room
            Resource meetingRoom = new Resource();
            meetingRoom.setName("Meeting Room");
            meetingRoom.setDescription("Small meeting room for 4-6 people");
            meetingRoom.setPrice(new BigDecimal("30.00"));
            meetingRoom.setIsAvailable(true);
            resourceRepository.save(meetingRoom);

            // AV Equipment
            Resource avEquipment = new Resource();
            avEquipment.setName("AV Equipment Set");
            avEquipment.setDescription("Complete audio-visual equipment package");
            avEquipment.setPrice(new BigDecimal("100.00"));
            avEquipment.setIsAvailable(true);
            resourceRepository.save(avEquipment);
        }
    }
}