package com.flagcamp.TripPlanner;

import com.flagcamp.TripPlanner.entity.UserEntity;
import com.flagcamp.TripPlanner.repository.TripPlanRepository;
import com.flagcamp.TripPlanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class DevRunner implements ApplicationRunner {


    private static final Logger logger = LoggerFactory.getLogger(DevRunner.class);


    private final TripPlanRepository tripPlanRepository;
    private final UserRepository userRepository;

    public DevRunner(TripPlanRepository tripPlanRepository, UserRepository userRepository) {
        this.tripPlanRepository = tripPlanRepository;
        this.userRepository = userRepository;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        UserEntity userInfo1 = new UserEntity(null,"123@gmail.com",true,"1234","felix","oo");
        UserEntity userInfo2 = new UserEntity(null,"qq3@gmail.com",true,"123224","nirvana","greenday");


        userRepository.save(userInfo1);
        userRepository.save(userInfo2);

    }
}


