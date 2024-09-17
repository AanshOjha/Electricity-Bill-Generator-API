package com.project.electricitybillgenerator;

import com.project.electricitybillgenerator.model.BillUser;
import com.project.electricitybillgenerator.repository.UserRepository;
import com.project.electricitybillgenerator.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    public void testRegisterUser() {
        BillUser user = new BillUser("Test", "Dubai, UAE", "0505", "ejjffy@gmail.com");
        when(userRepository.save(any(BillUser.class))).thenReturn(user);

        BillUser result = userService.saveUser(user);

        assertNotNull(result);
        assertEquals("Test", result.getName());
        verify(userRepository, times(1)).save(user);
    }
}
