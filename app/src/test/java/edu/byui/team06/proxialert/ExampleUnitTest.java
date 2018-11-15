package edu.byui.team06.proxialert;

import android.app.Application;
import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import androidx.test.core.app.ApplicationProvider;
import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.view.MainActivity;
import edu.byui.team06.proxialert.view.Notification;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

import android.content.ContentValues;
import android.provider.ContactsContract;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {



    @Test
    public void addition_isCorrect() { assertEquals(4, 2 + 2); }

    @Test
    public void multiplication_isCorrect() { assertEquals(100, 2 * 2 * 5); }

    @Test
    public void notificationIdCheck() {

        Notification n = mock(Notification.class);
        assertEquals(0, n.getId());
    }

    @Test
    public void taskCountCheck() {
        DatabaseHelper db = mock(DatabaseHelper.class);
        assertEquals(0, db.getTaskCount());
    }

    @Test
    //0.5 represents miles.
    public void getDist_isCorrect() { assertTrue(MainActivity.getDistance(0,0, 3, 4, 5.0f));}

    //@Test
   // public void latAndLongFormatting() { assertThat(4.2f, );}


}