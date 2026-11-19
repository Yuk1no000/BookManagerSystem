-- 交付演示数据：仅包含虚构图书，不导出本机数据库或真实账号信息。
-- 先执行 db.sql；也可由 compose.demo.yaml 在空数据库首次启动时自动导入。
-- 按书名和作者判断是否存在，可重复导入，不覆盖同名同作者的已有记录。
SET NAMES utf8mb4;
USE book_manage_system;

START TRANSACTION;

INSERT INTO books (name, author, price, description)
SELECT '演示图书：Java入门', '示例作者甲', 39.90, '用于演示图书列表、编号查询及信息更新。'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM books WHERE name = '演示图书：Java入门' AND author = '示例作者甲'
);

INSERT INTO books (name, author, price, description)
SELECT '演示图书：Vue实践', '示例作者乙', 49.00, '用于演示前端页面展示、表单校验及角色权限。'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM books WHERE name = '演示图书：Vue实践' AND author = '示例作者乙'
);

INSERT INTO books (name, author, price, description)
SELECT '演示图书：数据库基础', '示例作者丙', 59.80, '用于演示数据库持久化及删除前的二次确认。'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM books WHERE name = '演示图书：数据库基础' AND author = '示例作者丙'
);

COMMIT;
