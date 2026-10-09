# Sequential Fourm: raw-dump byte layout notes

`SequentialFourm.java`'s `PARAMETERS[]` table generally follows the rule
`byte offset = NRPN# - 1`, matching the row order in the Fourm MIDI
Implementation doc. That rule does not hold everywhere. The deviations below
were found by decoding a 512-patch hardware bulk dump (program dumps, type
0x02, unpacked with `unpackFrom7Bit()` at offset 6) and comparing each
parameter's actual value distribution across all 512 patches to its declared
range in `PARAMETERS[]`/the widget that displays it.

## Offsets holding a different parameter than the doc's row order implies

Already corrected in `PARAMETERS[]`.

| Offsets | Actual contents |
|---|---|
| 16, 17 | `feedbacklevel`, `feedbackon` |
| 18, 19 | `noiselevel`, `noiseon` |
| 29, 30 | `filterkeyamt`, `filterkeytrack` |
| 43, 45 | `filtenvrelease`, `filtenvsustain` |
| 47, 52 | `filtenvamt`, `filtenvvelon` |
| 49, 51 | `envretrig`, `filtenvvelamt` |
| 57, 58, 59 | `unisondetune`, `unisonon`, `unisonvoices` (3-way rotation) |
| 64, 67 | `modsrcfiltenvamt`, `modsrcfiltenvroute` |
| 86, 87 | `clockdiv`, `clockbpm` |
| 110, 111 | `arprange`, `arpmode` |

## Bit-packed parameters

The documented value occupies only the low bits of its byte; the rest is an
unidentified value. It's preserved round-trip via a `<key>hibits` shadow
model key rather than discarded — see `MASKED_PARAMS` in `SequentialFourm.java`.
`lfofreqsync`'s low nibble is confirmed exactly by the user guide's 16-entry
`LFO Sync Freq` option list (32/16/8/6/4/3/2/1.5/1/2:3/1:2/1:3/1:4/1:6/1:8/1:16
Steps).

| Parameter(s) | Bits used |
|---|---|
| `osc1sync`, `arpon`, `lforevsaw` | bit 0 |
| `noisetype`, `moddstfreqasrc`, `seqplaymode` | low 2 bits |
| `pitchbendrange`, `lfofreqsync` | low nibble |

## Chooser option names corrected against the Fourm User Guide (v1.2)

These didn't show up as `revise()` warnings — the stored *values* were always
in range, only the *displayed labels* were wrong (guessed by an earlier
session without a confirmed source). Fixed against the guide's "Param Menu"
appendix:

| Array | Confirmed via guide |
|---|---|
| `ARP_MODES` | Up, Down, Up+Down1, Up+Down2, Random, Assign, Seq Note, Seq Mod |
| `ARP_REPEATS` | Off, 1, 2, 3 |
| `SEQ_PLAY_MODES` | Retrig, Continue, One Shot, Step |
| `SEQ_MOD_DESTS` | Osc Freq A/B, Filter Cutoff, MOD 1/2/3 Amt, LFO Freq, Pulse Width A/B/All, Feedback (11, not 7) |
| `CATEGORIES` | Misc, Pad, Lead, Bass, Poly, Keys, String, Pluck, Bell, Arp, Brass, Voice, Organ, Percussion, Tuned Percussion, SFX (16, not 17 — see Unresolved) |
| `LFO_SH_TYPES` | S/H, Random, Pink, White, Violet, DC |

`GLIDE_MODES` and `ARP_RANGES` were checked against the guide and are already
correct.

## Range corrections (declared range was simply too narrow)

| Parameter | Was | Now |
|---|---|---|
| `filterkeyamt` | 0-127 | 0-255 (data continues smoothly past 127, like the ModSrc `*amt` params) |
| `unisonnote1`-`4` | 0-60 | 0-127, with 127 mapped to "Off" |

## Sentinel values

- Sequencer step `Note` and `Velocity` bytes use 128 (0x80) for "step inactive"
  (dials widened to 0-128, mapped to "Off"). Note redundant with the
  separately-tracked Rest flag.
- `unisonnote1`-`4`: value 127 means "this unison voice slot isn't in use"
  (confirmed by cross-referencing against `unisonvoices`: note2/3/4 are ~127
  almost exclusively at low voice counts, and increasingly show real values
  as `unisonvoices` approaches its max).

## Unresolved (checked against both the MIDI implementation doc and the User Guide — still open)

- **`clockdiv`**: the User Guide's own reference table (Clock chapter) lists
  exactly 11 divisions — 1/2, 1/4, 1/8, 1/8D, 1/8S, 1/8T, 1/16, 1/16S, 1/16T,
  1/32, 1/64 — matching `CLOCK_DIVS`'s current 11 entries exactly. Real
  hardware data nonetheless reaches 16 (54/512 patches, 10.5%). Since the
  *official, current* documentation doesn't support a 12th–17th division,
  this looks like a firmware-vs-manual version gap (firmware may have added
  divisions after User Guide v1.2 was published) rather than a mistake on our
  side. Not fixed — would need Sequential support or a newer manual.
- **`keymode`**: "Key Mode" does not appear anywhere in either document under
  that name or an obvious synonym (checked for Key Priority, Key Assign, Voice
  Mode, Play Mode, Poly/Mono, Legato — none match). The current `KEY_MODES =
  {"Poly","Mono","Unison"}` guess (from an earlier session) is unconfirmed by
  any source, and real data needs a 4th value (0-3, not 0-2) on top of that.
  Left entirely as-is rather than compounding one guess with another.
- **`noisetype`**: the guide names only 3 types (White, Pink, Violet) with no
  mention of a 4th, but real data cleanly needs 4 values (masked to low 2
  bits) with value 3 dominant in 86% of patches — implausible for a supposedly
  invalid state. `NOISE_TYPES`'s current 4th entry ("Red") is an unconfirmed
  guess; likely wrong name, unclear what's actually right.
- A handful of one-off outliers (`arprelatch`=3, a couple of `seqXnoteY`/`seqXvelY`
  values like 254/255/179/181) affect 2-12 patches out of 512 (under 2.5%
  each) with no discernible pattern — not chased further, low risk of being
  anything systematic.
- **`MOD_SRC_ROUTES` (Off/Pos/Full) and `MOD_DST_SRCS` (Off/Filter Env/Osc
  B/LFO)**: the guide's modulation-routing explanation (Low Frequency
  Oscillator chapter) describes something that may not match either name set
  — each of the 3 sources routes through a "direct bus" or "Mod Wheel bus"
  (2 buses, pressed 1 or 2 times), and each of the 8 destinations picks up
  direct/Mod-Wheel/both (3 states + off). That's a different mental model
  than "which of 3 sources" or "Off/Pos/Full" polarity, though it may just be
  a different description of the same underlying stored value. Not changed —
  didn't want to replace one unconfirmed guess with another possibly-wrong
  one. Worth re-reading the guide's Modulation section (~p.31-33) end to end,
  or testing on hardware, before touching these.
