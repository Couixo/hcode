use ring::digest;  // 替代md5库
use num_cpus;
use std::sync::{Arc, atomic::{AtomicBool, Ordering}};
use std::thread;

fn main() {
    const TARGET: u32 = 0x296661;
    const SUFFIX: &str = "334928";
    const CHARS: &str = "abcdefghijklmnopqrstuvwxyz0123456789";
    const MAX_SALT_LEN: usize = 5;

    let char_map = [
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    ];

    let thread_count = num_cpus::get();
    let found = Arc::new(AtomicBool::new(false));
    let mut handles = vec![];

    for thread_id in 0..thread_count {
        let found = Arc::clone(&found);
        let chars = CHARS.to_string();
        let suffix = SUFFIX.to_string();

        handles.push(thread::spawn(move || {
            crack_thread(
                thread_id,
                thread_count,
                &chars,
                MAX_SALT_LEN,
                &suffix,
                TARGET,
                &char_map,
                &found,
            );
        }));
    }

    for handle in handles {
        handle.join().unwrap();
    }

    if !found.load(Ordering::SeqCst) {
        println!("未找到匹配的盐值");
    }
}

fn crack_thread(
    thread_id: usize,
    thread_count: usize,
    chars: &str,
    max_salt_len: usize,
    suffix: &str,
    target: u32,
    char_map: &[char],
    found: &AtomicBool,
) {
    let chars: Vec  = chars.chars().collect();
    let total_combinations = chars.len().pow(max_salt_len as u32);

    for idx in 0..total_combinations {
        if found.load(Ordering::SeqCst) {
            return;
        }
        if idx % thread_count != thread_id {
            continue;
        }

        let mut salt = Vec::with_capacity(max_salt_len);
        let mut temp = idx;
        for _ in 0..max_salt_len {
            salt.push(chars[temp % chars.len()]);
            temp /= chars.len();
            if temp == 0 {
                break;
            }
        }
        let salt: String = salt.into_iter().collect();

        // 使用ring库计算MD5
        let input = format!("{}{}", salt, suffix);
        let hash = digest::digest(&digest::MD5, input.as_bytes());
        let hash_bytes = hash.as_ref();

        let mut translated = [0u8; 16];
        for i in 0..8 {
            translated[2 * i] = char_map[(hash_bytes[i] >> 4) as usize] as u8;
            translated[2 * i + 1] = char_map[(hash_bytes[i] & 0x0F) as usize] as u8;
        }

        let prefix = (translated[0] as u32) << 16 | (translated[1] as u32) << 8 | (translated[2] as u32);

        if prefix == target {
            if found.swap(true, Ordering::SeqCst) {
                return;
            }
            println!("找到盐值: {}", salt);
            return;
        }
    }
}
