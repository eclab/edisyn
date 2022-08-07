/** From Page 76 of the sysex specification */

#include <stdlib.h>
#include <stdio.h>

int fil_freq (int input, int maxfreq, int mul)
{
   int f = maxfreq;
   input = 255 - input;
   while (input-- > 0)
	 f *= mul, f /= 1024;
   return f;
}

void cnv_morph_gain (int input, char *buf)
{
    int gain10x = -240 + ((input * 120) / 32);
    int gain_i  = gain10x / 10;
    int gain_f  = abs (gain10x % 10);
    sprintf (buf, "%s%d.%1d dB",
			gain10x >= 0 ? "+" : "-",
			abs (gain_i),
			gain_f);
}


int main()
{
char foo[16];
for(int x = 0; x < 256; x++)
	{
	printf("%d Hz\n", fil_freq(x, 10000, 1006));
	}
return 0;
}

