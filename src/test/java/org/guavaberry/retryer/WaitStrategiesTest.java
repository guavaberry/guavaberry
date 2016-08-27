package org.guavaberry.retryer;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.rholder.retry.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class WaitStrategiesTest {
    @Mock
    private Attempt attemptMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void exponentialJitterWaitFactories() throws Exception {
        WaitStrategies.exponentialJitterWait();
        WaitStrategies.exponentialJitterWait(Duration.ZERO);
        WaitStrategies.exponentialJitterWait(Duration.ZERO, Duration.ZERO);
        WaitStrategies.exponentialJitterWait(Duration.ZERO, 1.0D);
        WaitStrategies.exponentialJitterWait(Duration.ZERO, Duration.ZERO, 1.0D);
    }

    @Test
    public void exponentialJitterWait() throws Exception {
        when(attemptMock.getAttemptNumber()).thenReturn(3L);
        WaitStrategy ws = WaitStrategies.exponentialJitterWait(Duration.ofSeconds(2L), Duration.ofSeconds(20L));
        assertThat(ws.computeSleepTime(attemptMock)).isBetween(4000L, 8000L);
    }

    @Test(expected = NullPointerException.class)
    public void exponentialWaitNullBaseTimeout() throws Exception {
        WaitStrategies.exponentialWait(null, Duration.ZERO);
    }

    @Test(expected = NullPointerException.class)
    public void exponentialWaitNullMaxTimeout() throws Exception {
        WaitStrategies.exponentialWait(Duration.ZERO, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exponentialWaitNegativeBaseTimeout() throws Exception {
        WaitStrategies.exponentialWait(Duration.ofMillis(-1L), Duration.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exponentialWaitNegativeMaxTimeout() throws Exception {
        WaitStrategies.exponentialJitterWait(Duration.ZERO, Duration.ofMillis(-1L));
    }

    @Test
    public void exponentialWaitFactories() throws Exception {
        WaitStrategies.exponentialWait(Duration.ZERO);
        WaitStrategies.exponentialWait(Duration.ZERO, Duration.ZERO);
    }

    @Test(expected = NullPointerException.class)
    public void exponentialWaitNullAttempt() throws Exception {
        WaitStrategy ws = WaitStrategies.exponentialWait(Duration.ofSeconds(1L), Duration.ofSeconds(50L));
        ws.computeSleepTime(null);
    }

    @Test
    public void exponentialWaitGreaterMaxTimeout() throws Exception {
        when(attemptMock.getAttemptNumber()).thenReturn(4L);
        WaitStrategy ws = WaitStrategies.exponentialWait(Duration.ofSeconds(1L), Duration.ofSeconds(5L));
        assertThat(ws.computeSleepTime(attemptMock)).isEqualTo(5000L);
    }

    @Test
    public void exponentialWaitAttemptToZero() throws Exception {
        when(attemptMock.getAttemptNumber()).thenReturn(0L);
        WaitStrategy ws = WaitStrategies.exponentialWait(Duration.ofSeconds(1L), Duration.ofSeconds(5L));
        assertThat(ws.computeSleepTime(attemptMock)).isEqualTo(500L);
    }

    @Test
    public void exponentialWait() throws Exception {
        when(attemptMock.getAttemptNumber()).thenReturn(4L);
        WaitStrategy ws = WaitStrategies.exponentialWait(Duration.ofSeconds(1L), Duration.ofSeconds(50L));
        assertThat(ws.computeSleepTime(attemptMock)).isEqualTo(8000L);
    }

    @Test(expected = NullPointerException.class)
    public void compositeJitterWaitNullStategy() throws Exception {
        WaitStrategies.compositeJitterWait(null, 1.0D);
    }

    @Test(expected = IllegalArgumentException.class)
    public void compositeJitterWaitNegativeFactor() throws Exception {
        WaitStrategies.compositeJitterWait(WaitStrategies.exponentialJitterWait(), -1.0D);
    }

    @Test(expected = IllegalArgumentException.class)
    public void compositeJitterWaitRandomFactorGreaterThanOne() throws Exception {
        WaitStrategies.compositeJitterWait(WaitStrategies.exponentialJitterWait(), 1.1D);
    }

    @Test(expected = NullPointerException.class)
    public void compositeJitterWaitNullAttempt() throws Exception {
        WaitStrategy ws = WaitStrategies.compositeJitterWait(
                com.github.rholder.retry.WaitStrategies.fixedWait(4, TimeUnit.SECONDS), 1.0D);
        ws.computeSleepTime(null);
    }

    @Test
    public void compositeJitterWaitNoRandomFactor() throws Exception {
        WaitStrategy ws = WaitStrategies.compositeJitterWait(
                com.github.rholder.retry.WaitStrategies.fixedWait(4, TimeUnit.SECONDS), 0.0D);
        assertEquals(4000, ws.computeSleepTime(attemptMock));
    }

    @Test
    public void compositeJitterWaitHalfRandomFactor() throws Exception {
        WaitStrategy ws = WaitStrategies.compositeJitterWait(
                com.github.rholder.retry.WaitStrategies.fixedWait(4, TimeUnit.SECONDS), 0.5D);
        assertThat(ws.computeSleepTime(attemptMock)).isBetween(2000L, 4000L);
    }

    @Test
    public void compositeJitterWaitFullRandomFactor() throws Exception {
        WaitStrategy ws = WaitStrategies.compositeJitterWait(
                com.github.rholder.retry.WaitStrategies.fixedWait(4, TimeUnit.SECONDS), 1.0D);
        assertThat(ws.computeSleepTime(attemptMock)).isBetween(0L, 4000L);
    }
}