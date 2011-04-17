package yamb.util.io;

class Padder
{
    private int maxPad;
    private String pads;

    public Padder(char padChar, int maxPad)
    {
        this.maxPad = maxPad;
        StringBuffer buf = new StringBuffer(maxPad);
        for (int i = 0; i < maxPad; i++)
        {
            buf.append(padChar);
        }
        pads = buf.toString();
    }

    public String pad(int inp)
    {
        return pad(String.valueOf(inp));
    }

    public String pad(String inp)
    {
        String ret = inp;
        if ((inp != null) && (inp.length() < maxPad))
        {
            ret = pads.substring(0, (maxPad - inp.length())) + inp;
        }

        return ret;
    }
}
