const std = @import("std");
const crypto = @import("crypto"); // 需安装：zig-pkg add crypto

pub fn main() !void {
    // 配置参数（可修改）
    const target: u32 = 0x296661; // 目标前缀（ASCII值）
    const suffix = "334928";       // 固定后缀
    const chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    const max_salt_len = 4;        // 盐值最大长度

    // 字符映射表（与原逻辑一致）
    const char_map = comptime blk: {
        var map: [256]u8 = undefined;
        for (0..256) |i| {
            map[i] = switch (@intToEnum(std.ascii.Lowercase, @intCast(u8, i))) {
                .a => '1', .b => '2', .c => '3',
                .d => '4', .e => '5', .f => '6',
                else => @intCast(u8, i),
            };
        }
        break :blk map;
    };

    // 多线程破解
    const thread_count = try std.Thread.getCpuCount();
    var threads: [thread_count]std.Thread = undefined;
    var found = std.atomic.Atomic(bool).init(false);

    for (0..thread_count) |i| {
        threads[i] = try std.Thread.spawn(.{}, crackThread, .{
            i, thread_count, chars, max_salt_len, suffix, target, &char_map, &found,
        });
    }

    for (threads) |*t| t.join();
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
    var salt_buf = allocator.alloc(u8, max_salt_len) catch return;
    defer allocator.free(salt_buf);

    // 遍历所有盐值组合（按线程分配任务）
    for (0..std.math.pow(usize, chars.len, max_salt_len)) |idx| {
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

        // 计算MD5哈希（依赖OpenSSL）
        var md5 = crypto.md.Md5.init(.{});
        md5.update(salt);
        md5.update(suffix);
        const hash = md5.final();

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
 
