package ch.hevs.cloudio.cloud.utils

import java.math.BigInteger
import java.util.*

fun UUID.toBigInteger(): BigInteger {
    var lo = BigInteger.valueOf(this.leastSignificantBits)
    var hi = BigInteger.valueOf(this.mostSignificantBits)
    if (lo.signum() < 0) lo = lo.add(BigInteger.ONE.shiftLeft(64))
    if (hi.signum() < 0) hi = hi.add(BigInteger.ONE.shiftLeft(64))
    return lo.add(hi.shiftLeft(64))
}
