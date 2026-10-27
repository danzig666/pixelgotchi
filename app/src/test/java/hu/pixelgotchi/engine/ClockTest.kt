package hu.pixelgotchi.engine

import hu.pixelgotchi.data.*
import org.junit.Assert.*
import org.junit.Test

class ClockTest {
    @Test fun monotonicIgnoresWallClockRollbackAndTimezoneChanges() {
        val a=ClockAnchor();val initial=ClockMath.advance(a,ClockReading(100_000,1000,1))
        assertEquals(initial+5000,ClockMath.advance(a,ClockReading(100,6000,1)))
        assertEquals(initial+6000,ClockMath.advance(a,ClockReading(999_999,7000,1)))
    }
    @Test fun rebootUsesUtcButNeverGoesBackward() {
        val a=ClockAnchor(100_000,9000,1)
        assertEquals(100_000,ClockMath.advance(a,ClockReading(100,10,2)))
        assertEquals(101_000,ClockMath.advance(a,ClockReading(200,1010,2)))
        assertEquals(200_000,ClockMath.advance(a,ClockReading(200_000,20,3)))
    }
}
