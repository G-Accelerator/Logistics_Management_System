package com.example.demo.config;

import com.example.demo.entity.UserAccount;
import com.example.demo.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动时写入默认管理员（仅当库中不存在任何 admin 账号时）
 */
@Component
public class UserAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserAccountInitializer.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountInitializer(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userAccountRepository.findByUsername("admin").isPresent()) {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("123456"));
        admin.setNickname("管理员");
        admin.setAvatar("");
        admin.setRole("admin");
        admin.setEnabled(true);
        userAccountRepository.save(admin);
        log.info("已初始化默认管理员账号 admin（请登录后在个人中心修改密码）");
    }
}
