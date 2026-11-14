# PESUGIHAN AI — Multi Store Analytics

Dashboard ringan untuk memantau hingga empat toko Shopee dalam satu tampilan.

## Fitur versi awal
- Ringkasan pesanan, omzet, dan kuantitas produk.
- Perbandingan performa empat toko.
- Filter periode dan toko.
- Impor laporan CSV dari Seller Centre.
- Analisis produk sederhana dan generator draft judul SEO.
- Target pesanan harian yang dapat diatur per toko.
- Ekspor kembali data yang sudah dimuat sebagai CSV.
- Penyimpanan lokal pada browser perangkat.

## Cara menggunakan
1. Buka dashboard melalui GitHub Pages setelah Pages diaktifkan pada repository.
2. Ekspor laporan penjualan dari Shopee Seller Centre dalam format CSV.
3. Pilih **Impor data** lalu unggah file CSV.
4. Pilih toko pada kolom toko/store di file. Jika laporan tidak menyertakan nama toko, beri kolom toko berisi 1, 2, 3, atau 4 sebelum impor.
5. Periksa angka dan hasil pemetaan setelah impor.

## Penting
Versi ini **belum terhubung langsung ke API Shopee**. Dashboard tidak meminta password, OTP, atau akses akun Shopee. Hasil analisis hanya seakurat data yang diimpor. Data disimpan di browser/perangkat yang digunakan; gunakan perangkat pribadi dan ekspor cadangan bila perlu.

Format CSV yang disarankan: `tanggal,toko,pesanan,produk,qty,omzet,status`.
