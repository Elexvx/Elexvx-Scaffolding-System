package elexvx.admin.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        String password = "123456";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Verify: " + BCrypt.checkpw(password, hash));
        
        // 测试数据库中的哈希
        String dbHash = "$2a$10$BbVSQCIChdR.4gfwiG1OduJiKE/KpUTbhBXd.7Sr.uwi8eggDpYeu";
        System.out.println("\nDB Hash verify: " + BCrypt.checkpw(password, dbHash));
    }
}
