/*
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp.ftp2;

import org.apache.commons.net.ftp.FTPFile;
import java.util.Vector;

/**
 * FTPFileIterator.java
 * This class implements a bidirectional iterator over an FTPFileList.
 * Elements may be retrieved one at at time using the hasNext() - next()
 * syntax familiar from Java 2 collections.  Alternatively, entries may
 * be receieved as an array of any requested number of entries or all of them.
 *
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: FTPFileIterator.java,v 1.9 2004/06/29 04:54:29 dfs Exp $
 * @see org.apache.commons.net.ftp.ftp2.FTPFileList
 */
public class FTPFileIterator
{
    private Vector rawlines;
    private FTPFileEntryParser parser;

    private static final int UNINIT = -1;
    private static final int DIREMPTY = -2;
    private int itemptr = 0;
    private int firstGoodEntry = UNINIT;

    /**
     * "Package-private" constructor.  Only the FTPFileList can
     * create an iterator, using it's iterator() method.  The list
     * will be iterated with the list's default parser.
     *
     * @param rawlist the FTPFileList to be iterated
     */
    FTPFileIterator ( FTPFileList rawlist )
    {
        this(rawlist, rawlist.getParser());
    }

    /**
     * "Package-private" constructor.  Only the FTPFileList can
     * create an iterator, using it's iterator() method.  The list will be
     * iterated with a supplied parser
     *
     * @param rawlist the FTPFileList to be iterated
     * @param parser the system specific parser for raw FTP entries.
     */
    FTPFileIterator ( FTPFileList rawlist, FTPFileEntryParser parser )
    {
        this.rawlines = rawlist.getLines();
        this.parser = parser;
    }

    private FTPFile parseFTPEntry(String entry)
    {
        return this.parser.parseFTPEntry(entry);
    }

    private int getFirstGoodEntry()
    {
        FTPFile entry = null;
        for (int iter = 0; iter < this.rawlines.size(); iter++)
        {
            String line = (String) this.rawlines.elementAt(iter);
            entry = parseFTPEntry(line);
            if (null != entry)
            {
                return iter;
            }
        }
        return DIREMPTY;
    }

    private void init()
    {
        this.itemptr = 0;
        this.firstGoodEntry = UNINIT;
    }

    private static final FTPFile[] EMPTY = new FTPFile[0];

    /**
     * Returns a list of FTPFile objects for ALL files listed in the server's
     * LIST output.
     *
     * @return a list of FTPFile objects for ALL files listed in the server's
     * LIST output.
     */
    public FTPFile[] getFiles()
    {
        if (this.itemptr != DIREMPTY)
        {
            init();
        }
        return getNext(0);
    }

    /**
     * Returns an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at this iterator's current position  within its 
     * associated list. If fewer than <code>quantityRequested</code> such 
     * elements are available, the returned array will have a length equal 
     * to the number of entries at and after after the current position.  
     * If no such entries are found, this array will have a length of 0.
     * 
     * After this method is called the current position is advanced by 
     * either <code>quantityRequested</code> or the number of entries 
     * available after the iterator, whichever is fewer.
     * 
     * @param quantityRequested
     * the maximum number of entries we want to get.  A 0
     * passed here is a signal to get ALL the entries.
     * 
     * @return an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at the current position of this iterator within its 
     * list and at least the number of elements which  exist in the list at 
     * and after its current position.
     */
    public FTPFile[] getNext(int quantityRequested)
    {

        // if we haven't gotten past the initial junk do so.
        if (this.firstGoodEntry == UNINIT)
        {
            this.firstGoodEntry = getFirstGoodEntry();
        }
        if (this.firstGoodEntry == DIREMPTY)
        {
            return EMPTY;
        }

        int max = this.rawlines.size() - this.firstGoodEntry;

        // now that we know the maximum we can possibly get,
        // resolve a 0 request to ask for that many.

        int howMany = (quantityRequested == 0) ? max : quantityRequested;
        howMany = (howMany + this.itemptr < this.rawlines.size())
                   ? howMany
                   : this.rawlines.size() - this.itemptr;

        FTPFile[] output = new FTPFile[howMany];

        for (int i = 0, e = this.firstGoodEntry + this.itemptr ;
                i < howMany; i++, e++)
        {
            output[i] = parseFTPEntry((String) this.rawlines.elementAt(e));
            this.itemptr++;

        }
        return output;
    }

    /**
     * Method for determining whether getNext() will successfully return a
     * non-null value.
     *
     * @return true if there exist any files after the one currently pointed
     * to by the internal iterator, false otherwise.
     */
    public boolean hasNext()
    {
        int fge = this.firstGoodEntry;
        if (fge == DIREMPTY)
        {
            //directory previously found empty - return false
            return false;
        }
        else if (fge < 0)
        {
            // we haven't scanned the list yet so do it first
            fge = getFirstGoodEntry();
        }
        return fge + this.itemptr < this.rawlines.size();
    }

    /**
     * Returns a single parsed FTPFile object corresponding to the raw input
     * line at this iterator's current position.
     *
     * After this method is called the internal iterator is advanced by one
     * element (unless already at end of list).
     *
     * @return a single FTPFile object corresponding to the raw input line
     * at the position of the internal iterator over the list of raw input
     * lines maintained by this object or null if no such object exists.
     */
    public FTPFile next()
    {
        FTPFile[] file = getNext(1);
        if (file.length > 0)
        {
            return file[0];
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at the position preceding this iterator's current 
     * position within its associated list. If fewer than 
     * <code>quantityRequested</code> such elements are available, the 
     * returned array will have a length equal to the number of entries after
     * the iterator.  If no such entries are found, this array will have a 
     * length of 0.  The entries will be ordered in the same order as the 
     * list, not reversed.
     *
     * After this method is called the current position is moved back by 
     * either <code>quantityRequested</code> or the number of entries 
     * available before the current position, whichever is fewer.
     * @param quantityRequested the maximum number of entries we want to get.  
     * A 0 passed here is a signal to get ALL the entries.
     * @return  an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at the position preceding the current position of 
     * this iterator within its list and at least the number of elements which
     * exist in the list prior to its current position.
     */
    public FTPFile[] getPrevious(int quantityRequested)
    {
        int howMany = quantityRequested;
        // can't retreat further than we've previously advanced
        if (howMany > this.itemptr)
        {
            howMany = this.itemptr;
        }
        FTPFile[] output = new FTPFile[howMany];
        for (int i = howMany, e = this.firstGoodEntry + this.itemptr; i > 0; )
        {
            output[--i] = parseFTPEntry((String) this.rawlines.elementAt(--e));
            this.itemptr--;
        }
        return output;
    }

    /**
     * Method for determining whether getPrevious() will successfully return a
     * non-null value.
     *
     * @return true if there exist any files before the one currently pointed
     * to by the internal iterator, false otherwise.
     */
    public boolean hasPrevious()
    {
        int fge = this.firstGoodEntry;
        if (fge == DIREMPTY)
        {
            //directory previously found empty - return false
            return false;
        }
        else if (fge < 0)
        {
            // we haven't scanned the list yet so do it first
            fge = getFirstGoodEntry();
        }

        return this.itemptr > fge;
    }

    /**
     * Returns a single parsed FTPFile object corresponding to the raw input
     * line at the position preceding that of the internal iterator over
     * the list of raw lines maintained by this object
     *
     * After this method is called the internal iterator is retreated by one
     * element (unless it is already at beginning of list).
     * @return a single FTPFile object corresponding to the raw input line
     * at the position immediately preceding that of the internal iterator
     * over the list of raw input lines maintained by this object.
     */
    public FTPFile previous()
    {
        FTPFile[] file = getPrevious(1);
        if (file.length > 0)
        {
            return file[0];
        }
        else
        {
            return null;
        }
    }
}