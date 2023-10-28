# DatecsPrinterDemoProjects

Datecs Java SDK: "fiscalprinterSDK.jar"

By differences into the commands - the fiscal devices of Datecs Ltd.can be separated in three different groups:
 - Group "A": FP-800, FP-2000, FP-650, SK1-21F, SK1-31F, FMP-10, FP-700
 - Group "B": DP-05, DP-05B, DP-15, DP-25, DP-35, WP-50, DP-150
 - Group "C": FMP-350X, FMP-55X, FP-700X, WP-500X, WP-50X, DP-25X, DP-150X, DP-05C, FP-700XE, FP-700MX, DP25MX, WP50MX, DP150MX

By differneces into the low-level packaged message - the fiscal devices of Datecs Ltd.can be separated in two different groups:
 - Group "V1": Protocol type one - the packaged message with 6 status bytes. All devices in group "A" and "B".
 - Group "V2": Protocol type two - the packaged message with 8 status bytes. All devices in group "C".



List of third-party librariesin folder ".\Lib":
-------------------------------------------------------------
jSerialComm-2.4.0.jar - Java library designed to provide a platform-independent way to access standard serial ports without requiring external libraries.
commons-csv-1.6.jar - "Commons CSV" reads and writes files in variations of the Comma Separated Value (CSV) format.

".\DemoProjects\DevicesGroup_AB" - demo project for devices in group "A" and "B" (protocol type "V1")
".\DemoProjects\DevicesGroup_C" - demo project for devices in group "A" and "B" (protocol type "V2")

