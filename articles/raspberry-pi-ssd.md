+++
title       = "Forays into using Raspberry Pi 4 as a home server"
published   = 2020-10-25
language    = "en-GB"
categories  = ["devops"]
description = "Discusses disk issues encountered while upgrading a Pi 3 to a Pi 4"
references  = "adpositions.toml"
+++

# Introduction
A few months ago, I upgraded my Raspberry Pi 3 home server to the latest model. With a faster CPU and significantly more RAM, the Pi 4 proves ideal for running networking services. Raspberry Pis are especially appealing as servers due to their compact design and low power consumption.

Over time, I kept adding more networking services to the Pi 4. Now, it hosts a number of different services, including NFS, Gitea, PostgreSQL, Docker Registry, Bitwarden and nginx. The old Pi 3 is still in use and functions as a secondary server for off-site backups.

This article discusses my Pi 4 setup and some of the issues I encountered.

# System setup
I installed [Alpine Linux 3.12](https://alpinelinux.org/) which is a lightweight and security-oriented Linux distribution. It features a recent 64-bit kernel (5.4.72-0-rpi4) and is straightforward to maintain. The system's boot and root partition reside on a 16 GB [MicroSDHC card](https://www.amazon.com/SanDisk-Ultra-MicroSDHC-Memory-Adapter/dp/B007XZL7PC).

Most of the services run as Docker containers to isolate them from the base system. All data for Docker are stored on an external SSD, a [SanDisk Ultra II SSD 240 GB](https://www.amazon.com/SanDisk-240GB-Solid-State-SDSSDA-240G-G26/dp/B01F9G43WU). For additional security, the ext4 partition on the SSD is encrypted via LUKS. The SSD is connected to an [ELUTENG USB 3.0 SATA Adapter](https://www.amazon.com/ELUTENG-Adapter-Support-Serial-Compatible/dp/B0716JKJ68) (`174c:55aa`) which is popular within the Raspberry Pi community.

Although the latest Pi 4 firmware supports booting from SSD, I decided to keep the root system on an SD card. This allows for quick hot-swapping in case of disk failures as we will see.

To run the simulations for my MSc thesis, I also overclocked the CPU to 2 GHz which improves the performance. A [heatsink](https://thepihut.com/products/aluminium-armour-heatsink-case-for-raspberry-pi-4) was necessary to keep the temperature under control.

# SSD problems
The entire setup has been running stable for two months — until today. I set out to copy 20 GB to the external drive via NFS. The `cp` command worked fine for a few minutes, but stopped suddenly. Checking the kernel logs with `dmesg`, I was presented with a long list of errors, the first ones being:

```
[347246.330762] usb 2-2: USB disconnect, device number 2
[347246.331921] sd 0:0:0:0: [sda] tag#11 uas_zap_pending 0 uas-tag 1 inflight: CMD
[347246.331928] sd 0:0:0:0: [sda] tag#11 CDB: opcode=0x2a 2a 00 13 e1 78 00 00 04 00 00
[347246.331934] sd 0:0:0:0: [sda] tag#10 uas_zap_pending 0 uas-tag 2 inflight: CMD
[347246.331938] sd 0:0:0:0: [sda] tag#10 CDB: opcode=0x2a 2a 00 13 e1 70 00 00 04 00 00
```

The messages do suggest a link to UAS which is known to be problematic. I henceforth [disabled it](https://www.raspberrypi.org/forums/viewtopic.php?f=28&t=245931&sid=520e811b346b9cbe4ae042f17ac901b1) and tried again.

At first, it seemed to have solved the problem. After waiting some more time, the transfer interrupted again, but now the errors looked rather different:

```
[ 1202.420030] sd 0:0:0:0: [sda] Synchronizing SCSI cache
[ 1202.426346] blk_update_request: I/O error, dev sda, sector 379438080 op 0x1:(WRITE) flags 0x4800 phys_seg 43 prio class 0
[ 1202.426378] blk_update_request: I/O error, dev sda, sector 379438592 op 0x1:(WRITE) flags 0x4800 phys_seg 64 prio class 0
[ 1202.426409] blk_update_request: I/O error, dev sda, sector 379439104 op 0x1:(WRITE) flags 0x4800 phys_seg 64 prio class 0
[ 1202.426514] sd 0:0:0:0: [sda] Synchronize Cache(10) failed: Result: hostbyte=0x01 driverbyte=0x00
```

After some more research, I found [this issue](https://github.com/raspberrypi/linux/issues/3404) which suggests that overclocking could be the root cause. However, more conservative settings (`arm_freq=1750`, `over_voltage=2`) did not improve the situation either.

Next, I decided to investigate whether a hardware fault could be at play since some users reported that "blk_update_request: I/O error" occurs with HDDs on x86 PCs. So I attached a different SSD ([Crucial BX500](https://www.amazon.com/Crucial-BX500-120GB-2-5-Inch-Internal/dp/B07G3KRZBY)) and copying 20 GB was indeed successful. It even worked with the CPU overclocked to 2 GHz (`over_voltage=6`, `arm_freq=2000`) and with UAS enabled.

Curiously, connected to a laptop, the faulty (?) SanDisk SSD did not exhibit any of the issues that occurred with the Pi 4. Sustained writes of 40 GB+ were unproblematic and `smartctl` did not report any suspicious values either.

# Conclusion
These findings demonstrate the importance of extensively testing storage before using it in production. External storage is notoriously finicky on single-board computers. While I had been running long simulations as well as various Docker services, none of these wrote sufficient data to trigger the erroneous disk behaviour. Gladly, the SSD was in intact after all and any lost critical data would have been backed up to a second Pi with a nightly job.
