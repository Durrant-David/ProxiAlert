package edu.byui.team06.proxialert;

import android.app.Notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.view.tasks.MainActivity;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {



    @Test
    public void addition_isCorrect() { assertEquals(4, 2 + 2); }

    @Test
    public void multiplication_isCorrect() { assertEquals(100, 2 * 2 * 5); }

    @Test
    public void taskCountCheck() {
        DatabaseHelper db = mock(DatabaseHelper.class);
        assertEquals(0, db.getTaskCount());
    }


    //@Test
   // public void latAndLongFormatting() { assertThat(4.2f, );}


}