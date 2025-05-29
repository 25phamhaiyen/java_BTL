package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Lớp hỗ trợ quản lý transaction, giúp đơn giản hóa quá trình xử lý giao dịch
 */
public class TransactionManager {
    
    /**
     * Thực thi một hành động trong một transaction
     * 
     * @param action Hành động cần thực thi
     * @throws SQLException Nếu có lỗi SQL
     */
    public static void executeWithTransaction(Consumer<Connection> action) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                action.accept(conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    
    /**
     * Thực thi một hành động trong một transaction và trả về kết quả
     * 
     * @param <T> Kiểu dữ liệu kết quả
     * @param action Hành động cần thực thi
     * @return Kết quả của hành động
     * @throws SQLException Nếu có lỗi SQL
     */
    public static <T> T executeWithTransactionResult(Function<Connection, T> action) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = action.apply(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}