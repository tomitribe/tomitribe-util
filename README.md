## tomitribe-util

A cornucopia of useful utility classes.

# Versions

* 1.x is compiled under JDK 8 but does not run after JDK 11 due to the use of Unsafe and ByteBuffer
* 2.x is compiled under JDK 11 and does support runtimes up to JDK 20 (time of writing this readme)
* 3.x will be migrated to Jakarta but in essence it's the same code as 2.x

# Jakarta compatible binaries

Until Jakarta becomes mainstream on this small repo, the conversion from javax to jakarta is done using the script `to-jakarta.sh`

To create a release, run the script from `master`, then push the new code to the `jakarta` branch and do the release from there.