package com.epam.gymappHibernate.services;


import com.epam.gymappHibernate.dao.TraineeRepository;
import com.epam.gymappHibernate.dao.UserRepository;
import com.epam.gymappHibernate.entity.Trainee;
import com.epam.gymappHibernate.entity.User;
import com.epam.gymappHibernate.util.PasswordGenerator;
import com.epam.gymappHibernate.util.UsernameGenerator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class TraineeService {
    @Autowired
    private final TraineeRepository traineeRepository;
    @Autowired
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);


    @Autowired
    public TraineeService(TraineeRepository traineeRepository, UserRepository userRepository) {
        this.traineeRepository = traineeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createTrainee(Trainee trainee) {

        List<String> existingUsernames = userRepository.getAllUsers();
        String username = UsernameGenerator.generateUsername(trainee.getUser().getFirstName(), trainee.getUser().getLastName(), existingUsernames);
        trainee.getUser().setUserName(username);
        String password = PasswordGenerator.generatePassword();
        trainee.getUser().setPassword(password);
        traineeRepository.saveTrainee(trainee);
        logger.info("Trainee created: {} ", username);
    }

    public boolean authenticate(String username, String password) {
        Trainee trainee = traineeRepository.getTraineeByUsername(username);
        if (trainee != null && trainee.getUser().getPassword().equals(password)) {
            logger.info("Authentication successful for user: {}", username);
            return true;
        } else {
            logger.warn("Authentication failed for user: {}", username);
            return false;
        }
    }

    @Transactional
    public void deleteTrainee(String username, String password) {
        if (authenticate(username, password)) {
            traineeRepository.deleteTraineeByUsername(username);
            logger.info("Deleted trainee: {}", username);
        } else {
            logger.error("Invalid username or password for trainee {}", username);
            throw new SecurityException("Invalid username or password");
        }

    }


    public Trainee getTraineeByUsername(String username, String password) {
        if (authenticate(username, password)) {
            logger.info("Selecting Trainee profile: {}", username);
            return traineeRepository.getTraineeByUsername(username);

        } else {
            logger.error("Invalid username or password for trainee {}", username);
            throw new SecurityException("Invalid username or password");
        }
    }

    @Transactional
    public void updateTraineeProfile(String username, String password, Trainee trainee) {
        if (authenticate(username, password)) {
            logger.info("Updating Trainee profile: {}", username);
            traineeRepository.updateTrainee(trainee);
        } else {
            logger.error("Invalid username or password for trainee {}", username);
            throw new SecurityException("Invalid username or password");
        }
    }

    @Transactional
    public void changeTraineePassword(String username, String newPassword, String password) {
        if (authenticate(username, password)) {
            Trainee trainee = traineeRepository.getTraineeByUsername(username);
            if (trainee != null) {
                logger.info("Changing Password");
                trainee.getUser().setPassword(newPassword);
                traineeRepository.updateTrainee(trainee);
            }
        } else {
            logger.error("Invalid username or password for trainee {}", username);
            throw new SecurityException("Invalid username or password");
        }
    }

    @Transactional
    public void setTraineeActiveStatus(String username, String password, boolean isActive) {
        logger.info("Setting active status for trainee: {}", username);
        if (authenticate(username, password)) {
            Trainee trainee = traineeRepository.getTraineeByUsername(username);
            if (trainee != null) {
                trainee.getUser().setActive(isActive);
                traineeRepository.updateTrainee(trainee);
                logger.info("Active status for trainee {} set to {}", username, isActive);
            } else {
                logger.warn("Trainee {} not found", username);
            }
        } else {
            logger.error("Invalid username or password for trainee {}", username);
            throw new SecurityException("Invalid username or password");
        }
    }

}
