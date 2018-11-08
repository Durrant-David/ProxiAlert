package edu.byui.team06.proxialert;

import org.junit.Test;
import static org.junit.Assert.*;

import edu.byui.team06.proxialert.view.MainActivity;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {assertEquals(4, 2 + 2);}

    @Test
    public void multiplication_isCorrect() {assertEquals(100, 2 * 2 * 5); }

    @Test
    //0.5 represents miles.
    public void getDist_isCorrect() { assertTrue(MainActivity.getDistance(0,0, 3, 4, 5.0f));}

    //@Test
   // public void latAndLongFormatting() { assertThat(4.2f, );}



}