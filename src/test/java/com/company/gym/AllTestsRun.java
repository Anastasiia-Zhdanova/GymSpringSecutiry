package com.company.gym;

import com.company.gym.config.*;
import com.company.gym.controller.AuthenticationControllerTest;
import com.company.gym.controller.TraineeControllerTest;
import com.company.gym.controller.TrainerControllerTest;
import com.company.gym.controller.TrainingControllerTest;
import com.company.gym.dao.*;
import com.company.gym.dto.request.LoginRequestTest;
import com.company.gym.dto.request.TrainingRequestTest;
import com.company.gym.dto.response.TrainingTypeResponseTest;
import com.company.gym.dto.response.UserCredentialsResponseTest;
import com.company.gym.entity.*;
import com.company.gym.exception.AuthenticationExceptionTest;
import com.company.gym.exception.GlobalExceptionHandlerTest;
import com.company.gym.exception.NotFoundExceptionTest;
import com.company.gym.exception.ValidationExceptionTest;
import com.company.gym.mapper.TraineeMapperTest;
import com.company.gym.mapper.TrainerMapperTest;
import com.company.gym.security.JwtService;
import com.company.gym.security.JwtServiceTest;
import com.company.gym.security.UserDetailsServiceImpl;
import com.company.gym.security.UserDetailsServiceImplTest;
import com.company.gym.service.*;
import com.company.gym.util.*;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        GenericDAOTest.class,
        TraineeDAOTest.class,
        TrainerDAOTest.class,
        TrainingDAOTest.class,
        TrainingTypeDAOTest.class,
        UserDAOTest.class,
        AuthServiceTest.class,
        TraineeServiceTest.class,
        TrainerServiceTest.class,
        TrainingServiceTest.class,
        TrainingTypeServiceTest.class,
        TraineeServiceFacadeTest.class,
        TrainerServiceFacadeTest.class,
        TraineeTest.class,
        TrainerTest.class,
        TrainingTest.class,
        TrainingTypeTest.class,
        UserTest.class,
        TraineeMapperTest.class,
        TrainerMapperTest.class,
        PasswordUtilTest.class,
        QueryUtilTest.class,
        UserCredentialGeneratorTest.class,
        UsernameUtilTest.class,
        AuthenticationExceptionTest.class,
        GlobalExceptionHandlerTest.class,
        NotFoundExceptionTest.class,
        ValidationExceptionTest.class,
        AuthenticationControllerTest.class,
        TraineeControllerTest.class,
        TrainerControllerTest.class,
        TrainingControllerTest.class,
        LoginRequestTest.class,
        TrainingRequestTest.class,
        TrainingTypeResponseTest.class,
        UserCredentialsResponseTest.class,
        WebSecurityConfigTest.class,
        LoggingAspectTest.class,
        JwtAuthenticationFilterTest.class,
        JwtServiceTest.class,
        UserDetailsServiceImplTest.class,
        TrainingTypeInitialLoadHealthIndicatorTest.class
})
public class AllTestsRun {

}