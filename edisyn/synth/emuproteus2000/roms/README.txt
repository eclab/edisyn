This is a collection of preset, instrument, arpeggio, and riff names for a great many ROM SIMMs available for the Proteus 2000 family and the Audity 2000.  Some ROMs that may be missing include:

    - SIMM Code AP530-01: "ISIS/XTREM", an expansion ROM for the Audity 2000 only (does not work on the Proteus 2000)

    - SIMM Code 18: "Extreme Lead V2 / XROM 1", the ROM found inside the E-Mu XK-6 Keyboard.

    - SIMM Code 19: "Pure Phatt / MROM 1", a variation of the Pure Phatt ROM, this time found inside the E-Mu MK-6 Keyboard.

    - SIMM Code 20: "Composer", a variation of the Composer ROM, this time found inside the Proteus 2500.

    - SIMM Code 22: "Sounds of the ZR / QROM", a variation of the ZR ROM, this time found inside the E-Mu Halo Keyboard.


This data is drawn from the Prodatum source distribution at https://sourceforge.net/projects/prodatum/files/ROM-Info/ with kind permission of Jan Mann (rdxesy@yahoo.de), who is the author of Prodatum.  Some cleanups were done.  For example SIMM Code 9 (World Expedition) had incorrect values for the back half of its instrument dataset, and was revised with values found at Emumania.net, with permission of the owner of that site.  The original is stored as "n_ins_9.txt.orig".  The same goes for n_prs_9.txt.

Jan's original Readme.txt file had a bit of context regarding the SIMM IDs, which I include below: 



################################################################################
ROM ID to ROM Name conversion (sorry for the bad formatting:):


const char* ROM::name() const
{
	pmesg(100, "name(code: %d)\n", id);
	switch (id)
	{
	case 0:
		return "User";
	case 2:
		return "XTREM";
	case 3:
		return "Audity";
	case 4:
		return "Composer";
	case 5:
		return "Protozoa";
	case 6:
		return "B3";
	case 7:
		return "XL-1";
	case 8:
		return "ZR-76";
	case 9:
		return "World Exp.";
	case 10:
		return "Orch 1";
	case 11:
		return "Orch 2";
	case 13:
	case 15:
		return "Mo'Phatt";
	case 14:
		return "XL-2";
	case 16:
		return "Ensoniq";
	case 17:
		return "PROM1";
	case 18:
		return "Vintage";
	case 19:
		return "DRUM";
	case 64:
		return "Holy Grail";
	case 65:
		return "TSCY";
	case 66:
		return "Siedlaczek";
	case 67:
		return "Beat";
	default:
		static char buf[20];
		snprintf(buf, 20, "Unknown (%d)", id);
		return buf;
	}
}

vvd_
