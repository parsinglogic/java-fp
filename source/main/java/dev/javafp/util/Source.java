/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

public class Source
{
    private final StringBuffer buffer;
    private long               timeout = 10000;

    public Source(StringBuffer buffer)
    {
        this.buffer = buffer;
    }

    public String read()
    {
        synchronized (buffer)
        {
            if (buffer.length() == 0)
            {
                //System.out.println("read is blocking");
                waitForBuffer();
                //System.out.println("read has woken up");
            }

            String message = buffer.toString();
            buffer.delete(0, buffer.length());
            return message;
        }
    }

    private void waitForBuffer()
    {
        waitForAWhile();
    }

    private void waitForAWhile()
    {
        long t1 = System.currentTimeMillis();
        try
        {
            buffer.wait(timeout);
        }
        catch (InterruptedException e)
        {

        }
        long t2 = System.currentTimeMillis();

        if (t2 - t1 > timeout - 500)
        {
            //System.out.println("Time waiting " + (t2 -t1));
            //System.out.println("Timed out");
            throw new RuntimeException("Read timed out");
        }
    }

    /**
     * <p> @return the timeout
     */
    public long getTimeout()
    {
        return timeout;
    }

    /**
     * <p> @param timeout the timeout to set
     */
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

}