#ifndef __KSU_H_AVC_SPOOF
#define __KSU_H_AVC_SPOOF

#include <linux/init.h>
#include <linux/types.h>

void __init ksu_avc_spoof_init(void);
void ksu_avc_spoof_late_init(void);
void __exit ksu_avc_spoof_exit(void);

void ksu_avc_spoof_enable(void);
void ksu_avc_spoof_disable(void);

#endif // __KSU_H_AVC_SPOOF
