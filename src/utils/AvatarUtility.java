package utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AvatarUtility {
    
    private static final String AVATAR_DIR = "avatars";
    private static final String[] SUPPORTED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
    
    /**
     * Lấy đường dẫn thư mục avatar trong resources (cùng cấp với src)
     */
    public static String getAvatarDirectory() {
        // Lấy đường dẫn resources cùng cấp với src
        String userDir = System.getProperty("user.dir");
        String avatarPath = userDir + File.separator + "resources" + File.separator + AVATAR_DIR;
        
        File directory = new File(avatarPath);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Đã tạo thư mục avatars tại: " + avatarPath);
        }
        
        return avatarPath + File.separator;
    }
    
    /**
     * Tìm file avatar theo username
     */
    public static File findAvatarFile(String username) {
        String avatarDir = getAvatarDirectory();
        
        for (String ext : SUPPORTED_EXTENSIONS) {
            File file = new File(avatarDir + username + ext);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }
    
    /**
     * Lưu avatar mới
     */
    public static boolean saveAvatar(File sourceFile, String username) {
        try {
            String extension = getFileExtension(sourceFile.getName());
            if (!isValidImageExtension(extension)) {
                throw new IOException("Định dạng file không được hỗ trợ: " + extension);
            }
            
            String avatarDir = getAvatarDirectory();
            
            // Xóa avatar cũ nếu có
            deleteOldAvatar(username);
            
            // Copy file mới
            Path sourcePath = sourceFile.toPath();
            Path targetPath = Paths.get(avatarDir + username + extension);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Đã lưu avatar tại: " + targetPath);
            System.out.println("Hãy refresh project trong IDE để thấy file mới!");
            
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu avatar: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Xóa avatar cũ
     */
    public static void deleteOldAvatar(String username) {
        String avatarDir = getAvatarDirectory();
        
        for (String ext : SUPPORTED_EXTENSIONS) {
            File oldFile = new File(avatarDir + username + ext);
            if (oldFile.exists()) {
                // Thử xóa nhiều lần nếu file bị lock
                boolean deleted = false;
                for (int i = 0; i < 3 && !deleted; i++) {
                    System.gc(); // Gọi garbage collector
                    try {
                        Thread.sleep(100); // Đợi 100ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    deleted = oldFile.delete();
                }
                
                if (!deleted) {
                    System.err.println("Không thể xóa file avatar cũ: " + oldFile.getAbsolutePath());
                }
            }
        }
    }
    
    /**
     * Đổi tên avatar khi username thay đổi - Phiên bản cải thiện với strategy pattern
     */
    public static boolean renameAvatar(String oldUsername, String newUsername) {
        File oldAvatarFile = findAvatarFile(oldUsername);
        if (oldAvatarFile == null) {
            return true; // Không có avatar cũ thì coi như thành công
        }
        
        // Strategy 1: Thử copy file trước, sau đó xóa file cũ
        if (copyThenDeleteStrategy(oldUsername, newUsername, oldAvatarFile)) {
            return true;
        }
        
        // Strategy 2: Thử move file trực tiếp
        if (moveFileStrategy(oldUsername, newUsername, oldAvatarFile)) {
            return true;
        }
        
        // Strategy 3: Thử rename file trực tiếp
        if (renameFileStrategy(oldUsername, newUsername, oldAvatarFile)) {
            return true;
        }
        
        System.err.println("Tất cả các phương thức đổi tên avatar đều thất bại");
        return false;
    }
    
    /**
     * Strategy 1: Copy file mới và xóa file cũ
     */
    private static boolean copyThenDeleteStrategy(String oldUsername, String newUsername, File oldAvatarFile) {
        try {
            String extension = getFileExtension(oldAvatarFile.getName());
            String avatarDir = getAvatarDirectory();
            
            Path oldPath = oldAvatarFile.toPath();
            Path newPath = Paths.get(avatarDir + newUsername + extension);
            
            // Copy file mới
            Files.copy(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Đã copy avatar thành công: " + newPath);
            
            // Xóa file cũ (với retry logic)
            boolean deleted = false;
            for (int i = 0; i < 5 && !deleted; i++) {
                System.gc(); // Force garbage collection
                try {
                    Thread.sleep(200 * (i + 1)); // Tăng dần thời gian chờ
                    deleted = Files.deleteIfExists(oldPath);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (IOException e) {
                    System.err.println("Lần thử " + (i + 1) + " xóa file thất bại: " + e.getMessage());
                }
            }
            
            if (deleted) {
                System.out.println("Đã xóa avatar cũ thành công: " + oldPath);
                return true;
            } else {
                System.err.println("Không thể xóa file avatar cũ: " + oldPath);
                // File mới đã được tạo, coi như thành công một phần
                return true;
            }
            
        } catch (IOException e) {
            System.err.println("Copy-then-delete strategy thất bại: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Strategy 2: Sử dụng Files.move()
     */
    private static boolean moveFileStrategy(String oldUsername, String newUsername, File oldAvatarFile) {
        try {
            String extension = getFileExtension(oldAvatarFile.getName());
            String avatarDir = getAvatarDirectory();
            
            Path oldPath = oldAvatarFile.toPath();
            Path newPath = Paths.get(avatarDir + newUsername + extension);
            
            // Thử move file
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Đã move avatar thành công từ: " + oldPath + " thành: " + newPath);
            return true;
            
        } catch (IOException e) {
            System.err.println("Move file strategy thất bại: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Strategy 3: Sử dụng File.renameTo() cũ
     */
    private static boolean renameFileStrategy(String oldUsername, String newUsername, File oldAvatarFile) {
        try {
            String extension = getFileExtension(oldAvatarFile.getName());
            String avatarDir = getAvatarDirectory();
            
            File newFile = new File(avatarDir + newUsername + extension);
            
            // Thử rename file
            boolean renamed = oldAvatarFile.renameTo(newFile);
            if (renamed) {
                System.out.println("Đã rename avatar thành công từ: " + oldAvatarFile.getAbsolutePath() + " thành: " + newFile.getAbsolutePath());
                return true;
            } else {
                System.err.println("File.renameTo() thất bại");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Rename file strategy thất bại: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy đường dẫn avatar resource để hiển thị (cho UI)
     */
    public static String getAvatarResourcePath(String username) {
        File avatarFile = findAvatarFile(username);
        if (avatarFile != null) {
            // Trả về đường dẫn tuyệt đối để load ảnh
            return avatarFile.getAbsolutePath();
        }
        return null;
    }
    
    /**
     * Lấy đường dẫn avatar resource để hiển thị (dạng resource path)
     */
    public static String getAvatarResource(String username) {
        File avatarFile = findAvatarFile(username);
        if (avatarFile != null) {
            String extension = getFileExtension(avatarFile.getName());
            return "resources/avatars/" + username + extension;
        }
        return "resources/images/avatar_placeholder.png";
    }
    
    /**
     * Lấy phần mở rộng của file
     */
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex).toLowerCase();
        }
        return "";
    }
    
    /**
     * Kiểm tra định dạng ảnh có hợp lệ không
     */
    private static boolean isValidImageExtension(String extension) {
        for (String validExt : SUPPORTED_EXTENSIONS) {
            if (validExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Lấy đường dẫn avatar resource (cho ảnh mặc định)
     */
    public static String getDefaultAvatarResource() {
        return "resources/images/avatar_placeholder.png";
    }
    
    /**
     * Kiểm tra xem avatar có tồn tại không
     */
    public static boolean hasAvatar(String username) {
        return findAvatarFile(username) != null;
    }
    
    /**
     * Debug method - in thông tin về avatar
     */
    public static void debugAvatarInfo(String username) {
        System.out.println("=== DEBUG AVATAR INFO ===");
        System.out.println("Username: " + username);
        System.out.println("Avatar directory: " + getAvatarDirectory());
        
        File avatarFile = findAvatarFile(username);
        if (avatarFile != null) {
            System.out.println("Avatar file found: " + avatarFile.getAbsolutePath());
            System.out.println("File exists: " + avatarFile.exists());
            System.out.println("File readable: " + avatarFile.canRead());
            System.out.println("File writable: " + avatarFile.canWrite());
        } else {
            System.out.println("No avatar file found for username: " + username);
        }
        System.out.println("=========================");
    }
}