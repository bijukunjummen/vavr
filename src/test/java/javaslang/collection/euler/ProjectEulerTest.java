/*     / \____  _    ______   _____ / \____   ____  _____
 *    /  \__  \/ \  / \__  \ /  __//  \__  \ /    \/ __  \   Javaslang
 *  _/  // _\  \  \/  / _\  \\_  \/  // _\  \  /\  \__/  /   Copyright 2014-2015 Daniel Dietrich
 * /___/ \_____/\____/\_____/____/\___\_____/_/  \_/____/    Licensed under the Apache License, Version 2.0
 */
package javaslang.collection.euler;

import javaslang.Function1;
import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.collection.Stream;
import javaslang.control.Match;
import org.junit.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>Contains high-level tests of the collection package.</p>
 * <p>See also <a href="https://projecteuler.net">Project Euler</a> and Pavel Fatin's
 * <a href="https://pavelfatin.com/ninety-nine">ninety-nine</a>.</p>
 */
public class ProjectEulerTest {

    /**
     * <strong>Problem 1: Multiples of 3 and 5</strong>
     * <p>If we list all the natural numbers below 10 that are multiples of 3 or 5, we get 3, 5, 6 and 9.
     * The sum of these multiples is 23.</p>
     * <p>Find the sum of all the multiples of 3 or 5 below 1000.</p>
     * <p>See also <a href="https://projecteuler.net/problem=1">projecteuler.net problem 1</a>.</p>
     */
    @Test
    public void shouldSolveProblem1() {
        assertThat(sumOfMultiplesOf3and5Below(10)).isEqualTo(23);
        assertThat(sumOfMultiplesOf3and5Below(1000)).isEqualTo(233168);
    }

    private static int sumOfMultiplesOf3and5Below(int limit) {
        return List.range(0, limit).filter(ProjectEulerTest::isMultipleOf3or5).sum().intValue();
    }

    private static boolean isMultipleOf3or5(int num) {
        return num != 0 && (num % 3 == 0 || num % 5 == 0);
    }

    /**
     * <strong>Problem 2: Even Fibonacci numbers</strong>
     * <p>Each new term in the Fibonacci sequence is generated by adding the previous
     * two terms. By starting with 1 and 2, the first 10 terms will be: 1, 2, 3, 5,
     * 8, 13, 21, 34, 55, 89, ...</p>
     * <p>By considering the terms in the Fibonacci sequence whose values do not exceed
     * four million, find the sum of the even-valued terms.</p>
     * <p>See also <a href="https://projecteuler.net/problem=2">projecteuler.net problem 2</a>.</p>
     */
    @Test
    public void shouldSolveProblem2() {
        assertThat(sumOfEvenFibonacciValuesNotExceeding(90)).isEqualTo(2 + 8 + 34);
        assertThat(sumOfEvenFibonacciValuesNotExceeding(4_000_000)).isEqualTo(4_613_732);
    }

    private static long sumOfEvenFibonacciValuesNotExceeding(final int max) {
        return Stream.from(2)
                .map(memoizedFibonacci)
                .takeWhile(f -> f <= max)
                .filter(f -> f % 2 == 0)
                .sum().longValue();
    }

    private static final Function<Integer, Long> memoizedFibonacci = Function1.lift(ProjectEulerTest::fibonacci).memoized();

    private static long fibonacci(int order) {
        return Match
                .when(0, i -> 0L)
                .when(1, i -> 1L)
                .when(2, i -> 1L)
                .otherwise(() -> memoizedFibonacci.apply(order - 2) + memoizedFibonacci.apply(order - 1))
                .apply(order);
    }

    /**
     * <strong>Problem 3: Largest prime factor</strong>
     * The prime factors of 13195 are 5, 7, 13 and 29.
     * <p>
     * What is the largest prime factor of the number 600851475143?
     * <p>
     * See also <a href="https://projecteuler.net/problem=3">projecteuler.net problem 3</a>.
     */
    @Test
    public void shouldSolveProblem3() {
        assertThat(largestPrimeFactorOf(24)).isEqualTo(3);
        assertThat(largestPrimeFactorOf(29)).isEqualTo(29);
        assertThat(largestPrimeFactorOf(13195)).isEqualTo(29);
        assertThat(largestPrimeFactorOf(600_851_475_143L)).isEqualTo(6857);
    }

    private static long largestPrimeFactorOf(long val) {
        return Stream.<Tuple2<Long, Long>>build(new Tuple2(1L, val), ProjectEulerTest::primeFactorsAndResultingValTail)
                .map(t -> t._1)
                .reduce(Math::max);
    }

    private static Stream<Tuple2<Long, Long>> primeFactorsAndResultingValTail(Tuple2<Long, Long> previousFactorAndResultingVal) {
        return Match
                .when(1L, p -> Stream.<Tuple2<Long, Long>>nil())
                .otherwise(() -> {
                    final long nextPrimeFactor = knownPrimes.filter(p -> previousFactorAndResultingVal._2 % p == 0).take(1).head();
                    return Stream.<Tuple2<Long, Long>>build(new Tuple2(nextPrimeFactor, previousFactorAndResultingVal._2 / nextPrimeFactor), ProjectEulerTest::primeFactorsAndResultingValTail);
                })
                .apply(previousFactorAndResultingVal._2);
    }

    /**
     * <strong>Problem 7: 10001st prime</strong>
     * <p>By listing the first six prime numbers: 2, 3, 5, 7, 11, and 13, we can see that the 6th prime is 13.</p>
     * <p>What is the 10 001st prime number?</p>
     * <p>See also <a href="https://projecteuler.net/problem=7">projecteuler.net problem 7</a>.</p>
     */
    @Test
    public void shouldSolveProblem7() {
        assertThat(primeNo(1)).isEqualTo(2);
        assertThat(primeNo(2)).isEqualTo(3);
        assertThat(primeNo(3)).isEqualTo(5);
        assertThat(primeNo(4)).isEqualTo(7);
        assertThat(primeNo(5)).isEqualTo(11);
        assertThat(primeNo(6)).isEqualTo(13);
        assertThat(primeNo(10_001)).isEqualTo(104_743);
    }

    private static long primeNo(int index) {
        if (index < 1) {
            throw new IllegalArgumentException("index < 1");
        }
        return knownPrimes.get(index - 1);
    }

    private static final Stream<Long> knownPrimes = Stream.gen(2L, p -> nextPrime(p));

    private static long nextPrime(long previousPrime) {
        return Match
                .when(2L, i -> 3L)
                .otherwise(() -> Stream.gen(previousPrime + 2, v -> v + 2)
                        .filter(i -> !isEvenlyDiversableByKnownPrimes(previousPrime, i))
                        .take(1).head())
                .apply(previousPrime);
    }

    private static boolean isEvenlyDiversableByKnownPrimes(long previousPrime, long val) {
        return knownPrimes.takeWhile(p -> p < previousPrime).append(previousPrime)
                .exists(p -> val % p == 0);
    }
}
