const std = @import("std");
const md5 = std.crypto.hash.Md5;

// 泛型字符映射函数（Zig 0.15+语法）
fn createCharMap(comptime EnumType: type) [256]u8 {
    var map: [256]u8 = undefined;
    for (0..256) |i| {
        const char = @intCast(u8, i);
        map[i] = switch (@enumFromInt(EnumType, char)) {
            .a => '1', .b => '2', .c => '3',
            .d => '4', .e => '5', .f => '6',
            else => char,
        };
    }
    return map;
}

pub fn main() !void {
    // 配置参数
    const target: u32 = 0x296661; // 目标前缀（ASCII值）
    const suffix = "334928";       // 固定后缀
    const chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    const max_salt_len = 5;        // 盐值最大长度

    // 编译期生成字符映射表
    const char_map = comptime createCharMap(std.ascii.Lowercase);

    // 多线程破解
    const thread_count = try std.Thread.getCpuCount();
    var threads = std.ArrayList(std.Thread).init(std.heap.page_allocator);
    defer threads.deinit();

    var found = std.atomic.Atomic(bool).init(false);

    for (0..thread_count) |i| {
        try threads.append(try std.Thread.spawn(.{}, crackThread, .{
            i, thread_count, chars, max_salt_len, suffix, target, &char_map, &found,
        }));
    }

    for (threads.items) |*t| t.join();
    if (!found.load(.SeqCst)) std.debug.print("未找到匹配的盐值\n", .{});
}

// 线程破解逻辑
fn crackThread(
    thread_id: usize,
    thread_count: usize,
    chars: []const u8,
    max_salt_len: usize,
    suffix: []const u8,
    target: u32,
    char_map: *const [256]u8,
    found: *std.atomic.Atomic(bool),
) void {
    const allocator = std.heap.page_allocator;
    const salt_buf = allocator.alloc(u8, max_salt_len) catch return;
    defer allocator.free(salt_buf);

    // 遍历所有盐值组合
    const total_combinations = std.math.pow(usize, chars.len, max_salt_len);
    for (0..total_combinations) |idx| {
        if (found.load(.SeqCst)) return;
        if (idx % thread_count != thread_id) continue;

        // 生成盐值（1-5位）
        var temp = idx;
        var salt_len: usize = 0;
        for (0..max_salt_len) |i| {
            salt_buf[i] = chars[temp % chars.len];
            temp /= chars.len;
            if (temp == 0) {
                salt_len = i + 1;
                break;
            }
        }
        const salt = salt_buf[0..salt_len];

        // 计算MD5哈希（使用Zig标准库）
        var hash: [md5.digest_length]u8 = undefined;
        md5.hash(salt ++ suffix, &hash, .{});

        // 转换哈希前缀并匹配
        var translated: [16]u8 = undefined;
        for (0..8) |i| {
            translated[2*i] = char_map[hash[i] >> 4];
            translated[2*i+1] = char_map[hash[i] & 0x0F];
        }
        const prefix = @as(u32, translated[0]) << 16 | @as(u32, translated[1]) << 8 | translated[2];

        if (prefix == target) {
            if (found.swap(true, .SeqCst)) return;
            std.debug.print("找到盐值: {s}\n", .{salt});
            return;
        }
    }
}
 
