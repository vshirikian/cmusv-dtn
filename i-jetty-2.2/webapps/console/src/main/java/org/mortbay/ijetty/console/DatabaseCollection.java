//========================================================================
//$Id: DatabaseCollection.java 354 2010-04-05 21:57:06Z joakim.erdfelt $
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.ijetty.console;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class DatabaseCollection
{
    protected Cursor cursor;

    public DatabaseCollection(Cursor cursor)
    {
        this.cursor = cursor;
    }

    public void close()
    {
        cursor.close();
    }

    public abstract ContentValues cursorToValues(Cursor cursor);

    public boolean hasNext()
    {
        return !cursor.isLast();
    }

    public ContentValues next()
    {
        ContentValues values = null;

        if (cursor.moveToNext())
        {
            return cursorToValues(cursor);
        }

        return values;
    }
}
