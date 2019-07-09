package uk.co.terminological.simplechart;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;


// https://cran.r-project.org/web/packages/ggsci/vignettes/ggsci.html

public class ColourScheme {

	public static ColourScheme Spectral = new ColourScheme("Spectral", new Colour[][]{{rgb(252,141,89), rgb(255,255,191), rgb(153,213,148)},{rgb(215,25,28), rgb(253,174,97), rgb(171,221,164), rgb(43,131,186)},{rgb(215,25,28), rgb(253,174,97), rgb(255,255,191), rgb(171,221,164), rgb(43,131,186)},{rgb(213,62,79), rgb(252,141,89), rgb(254,224,139), rgb(230,245,152), rgb(153,213,148), rgb(50,136,189)},{rgb(213,62,79), rgb(252,141,89), rgb(254,224,139), rgb(255,255,191), rgb(230,245,152), rgb(153,213,148), rgb(50,136,189)},{rgb(213,62,79), rgb(244,109,67), rgb(253,174,97), rgb(254,224,139), rgb(230,245,152), rgb(171,221,164), rgb(102,194,165), rgb(50,136,189)},{rgb(213,62,79), rgb(244,109,67), rgb(253,174,97), rgb(254,224,139), rgb(255,255,191), rgb(230,245,152), rgb(171,221,164), rgb(102,194,165), rgb(50,136,189)},{rgb(158,1,66), rgb(213,62,79), rgb(244,109,67), rgb(253,174,97), rgb(254,224,139), rgb(230,245,152), rgb(171,221,164), rgb(102,194,165), rgb(50,136,189), rgb(94,79,162)},{rgb(158,1,66), rgb(213,62,79), rgb(244,109,67), rgb(253,174,97), rgb(254,224,139), rgb(255,255,191), rgb(230,245,152), rgb(171,221,164), rgb(102,194,165), rgb(50,136,189), rgb(94,79,162)}}, "div");
	public static ColourScheme RdYlGn = new ColourScheme("RdYlGn", new Colour[][]{{rgb(252,141,89), rgb(255,255,191), rgb(145,207,96)},{rgb(215,25,28), rgb(253,174,97), rgb(166,217,106), rgb(26,150,65)},{rgb(215,25,28), rgb(253,174,97), rgb(255,255,191), rgb(166,217,106), rgb(26,150,65)},{rgb(215,48,39), rgb(252,141,89), rgb(254,224,139), rgb(217,239,139), rgb(145,207,96), rgb(26,152,80)},{rgb(215,48,39), rgb(252,141,89), rgb(254,224,139), rgb(255,255,191), rgb(217,239,139), rgb(145,207,96), rgb(26,152,80)},{rgb(215,48,39), rgb(244,109,67), rgb(253,174,97), rgb(254,224,139), rgb(217,239,139), rgb(166,217,106), rgb(102,189,99), rgb(26,152,80)},{rgb(215,48,39), rgb(244,109,67), rgb(253,174,97), rgb(254,224,139), rgb(255,255,191), rgb(217,239,139), rgb(166,217,106), rgb(102,189,99), rgb(26,152,80)},{rgb(165,0,38), rgb(215,48,39), rgb(244,109,67), rgb(253,174,97), rgb(254,224,139), rgb(217,239,139), rgb(166,217,106), rgb(102,189,99), rgb(26,152,80), rgb(0,104,55)},{rgb(165,0,38), rgb(215,48,39), rgb(244,109,67), rgb(253,174,97), rgb(254,224,139), rgb(255,255,191), rgb(217,239,139), rgb(166,217,106), rgb(102,189,99), rgb(26,152,80), rgb(0,104,55)}}, "div");
	public static ColourScheme RdBu = new ColourScheme("RdBu", new Colour[][]{{rgb(239,138,98), rgb(247,247,247), rgb(103,169,207)},{rgb(202,0,32), rgb(244,165,130), rgb(146,197,222), rgb(5,113,176)},{rgb(202,0,32), rgb(244,165,130), rgb(247,247,247), rgb(146,197,222), rgb(5,113,176)},{rgb(178,24,43), rgb(239,138,98), rgb(253,219,199), rgb(209,229,240), rgb(103,169,207), rgb(33,102,172)},{rgb(178,24,43), rgb(239,138,98), rgb(253,219,199), rgb(247,247,247), rgb(209,229,240), rgb(103,169,207), rgb(33,102,172)},{rgb(178,24,43), rgb(214,96,77), rgb(244,165,130), rgb(253,219,199), rgb(209,229,240), rgb(146,197,222), rgb(67,147,195), rgb(33,102,172)},{rgb(178,24,43), rgb(214,96,77), rgb(244,165,130), rgb(253,219,199), rgb(247,247,247), rgb(209,229,240), rgb(146,197,222), rgb(67,147,195), rgb(33,102,172)},{rgb(103,0,31), rgb(178,24,43), rgb(214,96,77), rgb(244,165,130), rgb(253,219,199), rgb(209,229,240), rgb(146,197,222), rgb(67,147,195), rgb(33,102,172), rgb(5,48,97)},{rgb(103,0,31), rgb(178,24,43), rgb(214,96,77), rgb(244,165,130), rgb(253,219,199), rgb(247,247,247), rgb(209,229,240), rgb(146,197,222), rgb(67,147,195), rgb(33,102,172), rgb(5,48,97)}}, "div");
	public static ColourScheme PiYG = new ColourScheme("PiYG", new Colour[][]{{rgb(233,163,201), rgb(247,247,247), rgb(161,215,106)},{rgb(208,28,139), rgb(241,182,218), rgb(184,225,134), rgb(77,172,38)},{rgb(208,28,139), rgb(241,182,218), rgb(247,247,247), rgb(184,225,134), rgb(77,172,38)},{rgb(197,27,125), rgb(233,163,201), rgb(253,224,239), rgb(230,245,208), rgb(161,215,106), rgb(77,146,33)},{rgb(197,27,125), rgb(233,163,201), rgb(253,224,239), rgb(247,247,247), rgb(230,245,208), rgb(161,215,106), rgb(77,146,33)},{rgb(197,27,125), rgb(222,119,174), rgb(241,182,218), rgb(253,224,239), rgb(230,245,208), rgb(184,225,134), rgb(127,188,65), rgb(77,146,33)},{rgb(197,27,125), rgb(222,119,174), rgb(241,182,218), rgb(253,224,239), rgb(247,247,247), rgb(230,245,208), rgb(184,225,134), rgb(127,188,65), rgb(77,146,33)},{rgb(142,1,82), rgb(197,27,125), rgb(222,119,174), rgb(241,182,218), rgb(253,224,239), rgb(230,245,208), rgb(184,225,134), rgb(127,188,65), rgb(77,146,33), rgb(39,100,25)},{rgb(142,1,82), rgb(197,27,125), rgb(222,119,174), rgb(241,182,218), rgb(253,224,239), rgb(247,247,247), rgb(230,245,208), rgb(184,225,134), rgb(127,188,65), rgb(77,146,33), rgb(39,100,25)}}, "div");
	public static ColourScheme PRGn = new ColourScheme("PRGn", new Colour[][]{{rgb(175,141,195), rgb(247,247,247), rgb(127,191,123)},{rgb(123,50,148), rgb(194,165,207), rgb(166,219,160), rgb(0,136,55)},{rgb(123,50,148), rgb(194,165,207), rgb(247,247,247), rgb(166,219,160), rgb(0,136,55)},{rgb(118,42,131), rgb(175,141,195), rgb(231,212,232), rgb(217,240,211), rgb(127,191,123), rgb(27,120,55)},{rgb(118,42,131), rgb(175,141,195), rgb(231,212,232), rgb(247,247,247), rgb(217,240,211), rgb(127,191,123), rgb(27,120,55)},{rgb(118,42,131), rgb(153,112,171), rgb(194,165,207), rgb(231,212,232), rgb(217,240,211), rgb(166,219,160), rgb(90,174,97), rgb(27,120,55)},{rgb(118,42,131), rgb(153,112,171), rgb(194,165,207), rgb(231,212,232), rgb(247,247,247), rgb(217,240,211), rgb(166,219,160), rgb(90,174,97), rgb(27,120,55)},{rgb(64,0,75), rgb(118,42,131), rgb(153,112,171), rgb(194,165,207), rgb(231,212,232), rgb(217,240,211), rgb(166,219,160), rgb(90,174,97), rgb(27,120,55), rgb(0,68,27)},{rgb(64,0,75), rgb(118,42,131), rgb(153,112,171), rgb(194,165,207), rgb(231,212,232), rgb(247,247,247), rgb(217,240,211), rgb(166,219,160), rgb(90,174,97), rgb(27,120,55), rgb(0,68,27)}}, "div");
	public static ColourScheme RdYlBu = new ColourScheme("RdYlBu", new Colour[][]{{rgb(252,141,89), rgb(255,255,191), rgb(145,191,219)},{rgb(215,25,28), rgb(253,174,97), rgb(171,217,233), rgb(44,123,182)},{rgb(215,25,28), rgb(253,174,97), rgb(255,255,191), rgb(171,217,233), rgb(44,123,182)},{rgb(215,48,39), rgb(252,141,89), rgb(254,224,144), rgb(224,243,248), rgb(145,191,219), rgb(69,117,180)},{rgb(215,48,39), rgb(252,141,89), rgb(254,224,144), rgb(255,255,191), rgb(224,243,248), rgb(145,191,219), rgb(69,117,180)},{rgb(215,48,39), rgb(244,109,67), rgb(253,174,97), rgb(254,224,144), rgb(224,243,248), rgb(171,217,233), rgb(116,173,209), rgb(69,117,180)},{rgb(215,48,39), rgb(244,109,67), rgb(253,174,97), rgb(254,224,144), rgb(255,255,191), rgb(224,243,248), rgb(171,217,233), rgb(116,173,209), rgb(69,117,180)},{rgb(165,0,38), rgb(215,48,39), rgb(244,109,67), rgb(253,174,97), rgb(254,224,144), rgb(224,243,248), rgb(171,217,233), rgb(116,173,209), rgb(69,117,180), rgb(49,54,149)},{rgb(165,0,38), rgb(215,48,39), rgb(244,109,67), rgb(253,174,97), rgb(254,224,144), rgb(255,255,191), rgb(224,243,248), rgb(171,217,233), rgb(116,173,209), rgb(69,117,180), rgb(49,54,149)}}, "div");
	public static ColourScheme BrBG = new ColourScheme("BrBG", new Colour[][]{{rgb(216,179,101), rgb(245,245,245), rgb(90,180,172)},{rgb(166,97,26), rgb(223,194,125), rgb(128,205,193), rgb(1,133,113)},{rgb(166,97,26), rgb(223,194,125), rgb(245,245,245), rgb(128,205,193), rgb(1,133,113)},{rgb(140,81,10), rgb(216,179,101), rgb(246,232,195), rgb(199,234,229), rgb(90,180,172), rgb(1,102,94)},{rgb(140,81,10), rgb(216,179,101), rgb(246,232,195), rgb(245,245,245), rgb(199,234,229), rgb(90,180,172), rgb(1,102,94)},{rgb(140,81,10), rgb(191,129,45), rgb(223,194,125), rgb(246,232,195), rgb(199,234,229), rgb(128,205,193), rgb(53,151,143), rgb(1,102,94)},{rgb(140,81,10), rgb(191,129,45), rgb(223,194,125), rgb(246,232,195), rgb(245,245,245), rgb(199,234,229), rgb(128,205,193), rgb(53,151,143), rgb(1,102,94)},{rgb(84,48,5), rgb(140,81,10), rgb(191,129,45), rgb(223,194,125), rgb(246,232,195), rgb(199,234,229), rgb(128,205,193), rgb(53,151,143), rgb(1,102,94), rgb(0,60,48)},{rgb(84,48,5), rgb(140,81,10), rgb(191,129,45), rgb(223,194,125), rgb(246,232,195), rgb(245,245,245), rgb(199,234,229), rgb(128,205,193), rgb(53,151,143), rgb(1,102,94), rgb(0,60,48)}}, "div");
	public static ColourScheme RdGy = new ColourScheme("RdGy", new Colour[][]{{rgb(239,138,98), rgb(255,255,255), rgb(153,153,153)},{rgb(202,0,32), rgb(244,165,130), rgb(186,186,186), rgb(64,64,64)},{rgb(202,0,32), rgb(244,165,130), rgb(255,255,255), rgb(186,186,186), rgb(64,64,64)},{rgb(178,24,43), rgb(239,138,98), rgb(253,219,199), rgb(224,224,224), rgb(153,153,153), rgb(77,77,77)},{rgb(178,24,43), rgb(239,138,98), rgb(253,219,199), rgb(255,255,255), rgb(224,224,224), rgb(153,153,153), rgb(77,77,77)},{rgb(178,24,43), rgb(214,96,77), rgb(244,165,130), rgb(253,219,199), rgb(224,224,224), rgb(186,186,186), rgb(135,135,135), rgb(77,77,77)},{rgb(178,24,43), rgb(214,96,77), rgb(244,165,130), rgb(253,219,199), rgb(255,255,255), rgb(224,224,224), rgb(186,186,186), rgb(135,135,135), rgb(77,77,77)},{rgb(103,0,31), rgb(178,24,43), rgb(214,96,77), rgb(244,165,130), rgb(253,219,199), rgb(224,224,224), rgb(186,186,186), rgb(135,135,135), rgb(77,77,77), rgb(26,26,26)},{rgb(103,0,31), rgb(178,24,43), rgb(214,96,77), rgb(244,165,130), rgb(253,219,199), rgb(255,255,255), rgb(224,224,224), rgb(186,186,186), rgb(135,135,135), rgb(77,77,77), rgb(26,26,26)}}, "div");
	public static ColourScheme PuOr = new ColourScheme("PuOr", new Colour[][]{{rgb(241,163,64), rgb(247,247,247), rgb(153,142,195)},{rgb(230,97,1), rgb(253,184,99), rgb(178,171,210), rgb(94,60,153)},{rgb(230,97,1), rgb(253,184,99), rgb(247,247,247), rgb(178,171,210), rgb(94,60,153)},{rgb(179,88,6), rgb(241,163,64), rgb(254,224,182), rgb(216,218,235), rgb(153,142,195), rgb(84,39,136)},{rgb(179,88,6), rgb(241,163,64), rgb(254,224,182), rgb(247,247,247), rgb(216,218,235), rgb(153,142,195), rgb(84,39,136)},{rgb(179,88,6), rgb(224,130,20), rgb(253,184,99), rgb(254,224,182), rgb(216,218,235), rgb(178,171,210), rgb(128,115,172), rgb(84,39,136)},{rgb(179,88,6), rgb(224,130,20), rgb(253,184,99), rgb(254,224,182), rgb(247,247,247), rgb(216,218,235), rgb(178,171,210), rgb(128,115,172), rgb(84,39,136)},{rgb(127,59,8), rgb(179,88,6), rgb(224,130,20), rgb(253,184,99), rgb(254,224,182), rgb(216,218,235), rgb(178,171,210), rgb(128,115,172), rgb(84,39,136), rgb(45,0,75)},{rgb(127,59,8), rgb(179,88,6), rgb(224,130,20), rgb(253,184,99), rgb(254,224,182), rgb(247,247,247), rgb(216,218,235), rgb(178,171,210), rgb(128,115,172), rgb(84,39,136), rgb(45,0,75)}}, "div");
	public static ColourScheme Set2 = new ColourScheme("Set2", new Colour[][]{{rgb(102,194,165), rgb(252,141,98), rgb(141,160,203)},{rgb(102,194,165), rgb(252,141,98), rgb(141,160,203), rgb(231,138,195)},{rgb(102,194,165), rgb(252,141,98), rgb(141,160,203), rgb(231,138,195), rgb(166,216,84)},{rgb(102,194,165), rgb(252,141,98), rgb(141,160,203), rgb(231,138,195), rgb(166,216,84), rgb(255,217,47)},{rgb(102,194,165), rgb(252,141,98), rgb(141,160,203), rgb(231,138,195), rgb(166,216,84), rgb(255,217,47), rgb(229,196,148)},{rgb(102,194,165), rgb(252,141,98), rgb(141,160,203), rgb(231,138,195), rgb(166,216,84), rgb(255,217,47), rgb(229,196,148), rgb(179,179,179)}}, "qual");
	public static ColourScheme Accent = new ColourScheme("Accent", new Colour[][]{{rgb(127,201,127), rgb(190,174,212), rgb(253,192,134)},{rgb(127,201,127), rgb(190,174,212), rgb(253,192,134), rgb(255,255,153)},{rgb(127,201,127), rgb(190,174,212), rgb(253,192,134), rgb(255,255,153), rgb(56,108,176)},{rgb(127,201,127), rgb(190,174,212), rgb(253,192,134), rgb(255,255,153), rgb(56,108,176), rgb(240,2,127)},{rgb(127,201,127), rgb(190,174,212), rgb(253,192,134), rgb(255,255,153), rgb(56,108,176), rgb(240,2,127), rgb(191,91,23)},{rgb(127,201,127), rgb(190,174,212), rgb(253,192,134), rgb(255,255,153), rgb(56,108,176), rgb(240,2,127), rgb(191,91,23), rgb(102,102,102)}}, "qual");
	public static ColourScheme Set1 = new ColourScheme("Set1", new Colour[][]{{rgb(228,26,28), rgb(55,126,184), rgb(77,175,74)},{rgb(228,26,28), rgb(55,126,184), rgb(77,175,74), rgb(152,78,163)},{rgb(228,26,28), rgb(55,126,184), rgb(77,175,74), rgb(152,78,163), rgb(255,127,0)},{rgb(228,26,28), rgb(55,126,184), rgb(77,175,74), rgb(152,78,163), rgb(255,127,0), rgb(255,255,51)},{rgb(228,26,28), rgb(55,126,184), rgb(77,175,74), rgb(152,78,163), rgb(255,127,0), rgb(255,255,51), rgb(166,86,40)},{rgb(228,26,28), rgb(55,126,184), rgb(77,175,74), rgb(152,78,163), rgb(255,127,0), rgb(255,255,51), rgb(166,86,40), rgb(247,129,191)},{rgb(228,26,28), rgb(55,126,184), rgb(77,175,74), rgb(152,78,163), rgb(255,127,0), rgb(255,255,51), rgb(166,86,40), rgb(247,129,191), rgb(153,153,153)}}, "qual");
	public static ColourScheme Set3 = new ColourScheme("Set3", new Colour[][]{{rgb(141,211,199), rgb(255,255,179), rgb(190,186,218)},{rgb(141,211,199), rgb(255,255,179), rgb(190,186,218), rgb(251,128,114)},{rgb(141,211,199), rgb(255,255,179), rgb(190,186,218), rgb(251,128,114), rgb(128,177,211)},{rgb(141,211,199), rgb(255,255,179), rgb(190,186,218), rgb(251,128,114), rgb(128,177,211), rgb(253,180,98)},{rgb(141,211,199), rgb(255,255,179), rgb(190,186,218), rgb(251,128,114), rgb(128,177,211), rgb(253,180,98), rgb(179,222,105)},{rgb(141,211,199), rgb(255,255,179), rgb(190,186,218), rgb(251,128,114), rgb(128,177,211), rgb(253,180,98), rgb(179,222,105), rgb(252,205,229)},{rgb(141,211,199), rgb(255,255,179), rgb(190,186,218), rgb(251,128,114), rgb(128,177,211), rgb(253,180,98), rgb(179,222,105), rgb(252,205,229), rgb(217,217,217)},{rgb(141,211,199), rgb(255,255,179), rgb(190,186,218), rgb(251,128,114), rgb(128,177,211), rgb(253,180,98), rgb(179,222,105), rgb(252,205,229), rgb(217,217,217), rgb(188,128,189)},{rgb(141,211,199), rgb(255,255,179), rgb(190,186,218), rgb(251,128,114), rgb(128,177,211), rgb(253,180,98), rgb(179,222,105), rgb(252,205,229), rgb(217,217,217), rgb(188,128,189), rgb(204,235,197)},{rgb(141,211,199), rgb(255,255,179), rgb(190,186,218), rgb(251,128,114), rgb(128,177,211), rgb(253,180,98), rgb(179,222,105), rgb(252,205,229), rgb(217,217,217), rgb(188,128,189), rgb(204,235,197), rgb(255,237,111)}}, "qual");
	public static ColourScheme Dark2 = new ColourScheme("Dark2", new Colour[][]{{rgb(27,158,119), rgb(217,95,2), rgb(117,112,179)},{rgb(27,158,119), rgb(217,95,2), rgb(117,112,179), rgb(231,41,138)},{rgb(27,158,119), rgb(217,95,2), rgb(117,112,179), rgb(231,41,138), rgb(102,166,30)},{rgb(27,158,119), rgb(217,95,2), rgb(117,112,179), rgb(231,41,138), rgb(102,166,30), rgb(230,171,2)},{rgb(27,158,119), rgb(217,95,2), rgb(117,112,179), rgb(231,41,138), rgb(102,166,30), rgb(230,171,2), rgb(166,118,29)},{rgb(27,158,119), rgb(217,95,2), rgb(117,112,179), rgb(231,41,138), rgb(102,166,30), rgb(230,171,2), rgb(166,118,29), rgb(102,102,102)}}, "qual");
	public static ColourScheme Paired = new ColourScheme("Paired", new Colour[][]{{rgb(166,206,227), rgb(31,120,180), rgb(178,223,138)},{rgb(166,206,227), rgb(31,120,180), rgb(178,223,138), rgb(51,160,44)},{rgb(166,206,227), rgb(31,120,180), rgb(178,223,138), rgb(51,160,44), rgb(251,154,153)},{rgb(166,206,227), rgb(31,120,180), rgb(178,223,138), rgb(51,160,44), rgb(251,154,153), rgb(227,26,28)},{rgb(166,206,227), rgb(31,120,180), rgb(178,223,138), rgb(51,160,44), rgb(251,154,153), rgb(227,26,28), rgb(253,191,111)},{rgb(166,206,227), rgb(31,120,180), rgb(178,223,138), rgb(51,160,44), rgb(251,154,153), rgb(227,26,28), rgb(253,191,111), rgb(255,127,0)},{rgb(166,206,227), rgb(31,120,180), rgb(178,223,138), rgb(51,160,44), rgb(251,154,153), rgb(227,26,28), rgb(253,191,111), rgb(255,127,0), rgb(202,178,214)},{rgb(166,206,227), rgb(31,120,180), rgb(178,223,138), rgb(51,160,44), rgb(251,154,153), rgb(227,26,28), rgb(253,191,111), rgb(255,127,0), rgb(202,178,214), rgb(106,61,154)},{rgb(166,206,227), rgb(31,120,180), rgb(178,223,138), rgb(51,160,44), rgb(251,154,153), rgb(227,26,28), rgb(253,191,111), rgb(255,127,0), rgb(202,178,214), rgb(106,61,154), rgb(255,255,153)},{rgb(166,206,227), rgb(31,120,180), rgb(178,223,138), rgb(51,160,44), rgb(251,154,153), rgb(227,26,28), rgb(253,191,111), rgb(255,127,0), rgb(202,178,214), rgb(106,61,154), rgb(255,255,153), rgb(177,89,40)}}, "qual");
	public static ColourScheme Pastel2 = new ColourScheme("Pastel2", new Colour[][]{{rgb(179,226,205), rgb(253,205,172), rgb(203,213,232)},{rgb(179,226,205), rgb(253,205,172), rgb(203,213,232), rgb(244,202,228)},{rgb(179,226,205), rgb(253,205,172), rgb(203,213,232), rgb(244,202,228), rgb(230,245,201)},{rgb(179,226,205), rgb(253,205,172), rgb(203,213,232), rgb(244,202,228), rgb(230,245,201), rgb(255,242,174)},{rgb(179,226,205), rgb(253,205,172), rgb(203,213,232), rgb(244,202,228), rgb(230,245,201), rgb(255,242,174), rgb(241,226,204)},{rgb(179,226,205), rgb(253,205,172), rgb(203,213,232), rgb(244,202,228), rgb(230,245,201), rgb(255,242,174), rgb(241,226,204), rgb(204,204,204)}}, "qual");
	public static ColourScheme Pastel1 = new ColourScheme("Pastel1", new Colour[][]{{rgb(251,180,174), rgb(179,205,227), rgb(204,235,197)},{rgb(251,180,174), rgb(179,205,227), rgb(204,235,197), rgb(222,203,228)},{rgb(251,180,174), rgb(179,205,227), rgb(204,235,197), rgb(222,203,228), rgb(254,217,166)},{rgb(251,180,174), rgb(179,205,227), rgb(204,235,197), rgb(222,203,228), rgb(254,217,166), rgb(255,255,204)},{rgb(251,180,174), rgb(179,205,227), rgb(204,235,197), rgb(222,203,228), rgb(254,217,166), rgb(255,255,204), rgb(229,216,189)},{rgb(251,180,174), rgb(179,205,227), rgb(204,235,197), rgb(222,203,228), rgb(254,217,166), rgb(255,255,204), rgb(229,216,189), rgb(253,218,236)},{rgb(251,180,174), rgb(179,205,227), rgb(204,235,197), rgb(222,203,228), rgb(254,217,166), rgb(255,255,204), rgb(229,216,189), rgb(253,218,236), rgb(242,242,242)}}, "qual");
	public static ColourScheme OrRd = new ColourScheme("OrRd", new Colour[][]{{rgb(254,232,200), rgb(253,187,132), rgb(227,74,51)},{rgb(254,240,217), rgb(253,204,138), rgb(252,141,89), rgb(215,48,31)},{rgb(254,240,217), rgb(253,204,138), rgb(252,141,89), rgb(227,74,51), rgb(179,0,0)},{rgb(254,240,217), rgb(253,212,158), rgb(253,187,132), rgb(252,141,89), rgb(227,74,51), rgb(179,0,0)},{rgb(254,240,217), rgb(253,212,158), rgb(253,187,132), rgb(252,141,89), rgb(239,101,72), rgb(215,48,31), rgb(153,0,0)},{rgb(255,247,236), rgb(254,232,200), rgb(253,212,158), rgb(253,187,132), rgb(252,141,89), rgb(239,101,72), rgb(215,48,31), rgb(153,0,0)},{rgb(255,247,236), rgb(254,232,200), rgb(253,212,158), rgb(253,187,132), rgb(252,141,89), rgb(239,101,72), rgb(215,48,31), rgb(179,0,0), rgb(127,0,0)}}, "seq");
	public static ColourScheme PuBu = new ColourScheme("PuBu", new Colour[][]{{rgb(236,231,242), rgb(166,189,219), rgb(43,140,190)},{rgb(241,238,246), rgb(189,201,225), rgb(116,169,207), rgb(5,112,176)},{rgb(241,238,246), rgb(189,201,225), rgb(116,169,207), rgb(43,140,190), rgb(4,90,141)},{rgb(241,238,246), rgb(208,209,230), rgb(166,189,219), rgb(116,169,207), rgb(43,140,190), rgb(4,90,141)},{rgb(241,238,246), rgb(208,209,230), rgb(166,189,219), rgb(116,169,207), rgb(54,144,192), rgb(5,112,176), rgb(3,78,123)},{rgb(255,247,251), rgb(236,231,242), rgb(208,209,230), rgb(166,189,219), rgb(116,169,207), rgb(54,144,192), rgb(5,112,176), rgb(3,78,123)},{rgb(255,247,251), rgb(236,231,242), rgb(208,209,230), rgb(166,189,219), rgb(116,169,207), rgb(54,144,192), rgb(5,112,176), rgb(4,90,141), rgb(2,56,88)}}, "seq");
	public static ColourScheme BuPu = new ColourScheme("BuPu", new Colour[][]{{rgb(224,236,244), rgb(158,188,218), rgb(136,86,167)},{rgb(237,248,251), rgb(179,205,227), rgb(140,150,198), rgb(136,65,157)},{rgb(237,248,251), rgb(179,205,227), rgb(140,150,198), rgb(136,86,167), rgb(129,15,124)},{rgb(237,248,251), rgb(191,211,230), rgb(158,188,218), rgb(140,150,198), rgb(136,86,167), rgb(129,15,124)},{rgb(237,248,251), rgb(191,211,230), rgb(158,188,218), rgb(140,150,198), rgb(140,107,177), rgb(136,65,157), rgb(110,1,107)},{rgb(247,252,253), rgb(224,236,244), rgb(191,211,230), rgb(158,188,218), rgb(140,150,198), rgb(140,107,177), rgb(136,65,157), rgb(110,1,107)},{rgb(247,252,253), rgb(224,236,244), rgb(191,211,230), rgb(158,188,218), rgb(140,150,198), rgb(140,107,177), rgb(136,65,157), rgb(129,15,124), rgb(77,0,75)}}, "seq");
	public static ColourScheme Oranges = new ColourScheme("Oranges", new Colour[][]{{rgb(254,230,206), rgb(253,174,107), rgb(230,85,13)},{rgb(254,237,222), rgb(253,190,133), rgb(253,141,60), rgb(217,71,1)},{rgb(254,237,222), rgb(253,190,133), rgb(253,141,60), rgb(230,85,13), rgb(166,54,3)},{rgb(254,237,222), rgb(253,208,162), rgb(253,174,107), rgb(253,141,60), rgb(230,85,13), rgb(166,54,3)},{rgb(254,237,222), rgb(253,208,162), rgb(253,174,107), rgb(253,141,60), rgb(241,105,19), rgb(217,72,1), rgb(140,45,4)},{rgb(255,245,235), rgb(254,230,206), rgb(253,208,162), rgb(253,174,107), rgb(253,141,60), rgb(241,105,19), rgb(217,72,1), rgb(140,45,4)},{rgb(255,245,235), rgb(254,230,206), rgb(253,208,162), rgb(253,174,107), rgb(253,141,60), rgb(241,105,19), rgb(217,72,1), rgb(166,54,3), rgb(127,39,4)}}, "seq");
	public static ColourScheme BuGn = new ColourScheme("BuGn", new Colour[][]{{rgb(229,245,249), rgb(153,216,201), rgb(44,162,95)},{rgb(237,248,251), rgb(178,226,226), rgb(102,194,164), rgb(35,139,69)},{rgb(237,248,251), rgb(178,226,226), rgb(102,194,164), rgb(44,162,95), rgb(0,109,44)},{rgb(237,248,251), rgb(204,236,230), rgb(153,216,201), rgb(102,194,164), rgb(44,162,95), rgb(0,109,44)},{rgb(237,248,251), rgb(204,236,230), rgb(153,216,201), rgb(102,194,164), rgb(65,174,118), rgb(35,139,69), rgb(0,88,36)},{rgb(247,252,253), rgb(229,245,249), rgb(204,236,230), rgb(153,216,201), rgb(102,194,164), rgb(65,174,118), rgb(35,139,69), rgb(0,88,36)},{rgb(247,252,253), rgb(229,245,249), rgb(204,236,230), rgb(153,216,201), rgb(102,194,164), rgb(65,174,118), rgb(35,139,69), rgb(0,109,44), rgb(0,68,27)}}, "seq");
	public static ColourScheme YlOrBr = new ColourScheme("YlOrBr", new Colour[][]{{rgb(255,247,188), rgb(254,196,79), rgb(217,95,14)},{rgb(255,255,212), rgb(254,217,142), rgb(254,153,41), rgb(204,76,2)},{rgb(255,255,212), rgb(254,217,142), rgb(254,153,41), rgb(217,95,14), rgb(153,52,4)},{rgb(255,255,212), rgb(254,227,145), rgb(254,196,79), rgb(254,153,41), rgb(217,95,14), rgb(153,52,4)},{rgb(255,255,212), rgb(254,227,145), rgb(254,196,79), rgb(254,153,41), rgb(236,112,20), rgb(204,76,2), rgb(140,45,4)},{rgb(255,255,229), rgb(255,247,188), rgb(254,227,145), rgb(254,196,79), rgb(254,153,41), rgb(236,112,20), rgb(204,76,2), rgb(140,45,4)},{rgb(255,255,229), rgb(255,247,188), rgb(254,227,145), rgb(254,196,79), rgb(254,153,41), rgb(236,112,20), rgb(204,76,2), rgb(153,52,4), rgb(102,37,6)}}, "seq");
	public static ColourScheme YlGn = new ColourScheme("YlGn", new Colour[][]{{rgb(247,252,185), rgb(173,221,142), rgb(49,163,84)},{rgb(255,255,204), rgb(194,230,153), rgb(120,198,121), rgb(35,132,67)},{rgb(255,255,204), rgb(194,230,153), rgb(120,198,121), rgb(49,163,84), rgb(0,104,55)},{rgb(255,255,204), rgb(217,240,163), rgb(173,221,142), rgb(120,198,121), rgb(49,163,84), rgb(0,104,55)},{rgb(255,255,204), rgb(217,240,163), rgb(173,221,142), rgb(120,198,121), rgb(65,171,93), rgb(35,132,67), rgb(0,90,50)},{rgb(255,255,229), rgb(247,252,185), rgb(217,240,163), rgb(173,221,142), rgb(120,198,121), rgb(65,171,93), rgb(35,132,67), rgb(0,90,50)},{rgb(255,255,229), rgb(247,252,185), rgb(217,240,163), rgb(173,221,142), rgb(120,198,121), rgb(65,171,93), rgb(35,132,67), rgb(0,104,55), rgb(0,69,41)}}, "seq");
	public static ColourScheme Reds = new ColourScheme("Reds", new Colour[][]{{rgb(254,224,210), rgb(252,146,114), rgb(222,45,38)},{rgb(254,229,217), rgb(252,174,145), rgb(251,106,74), rgb(203,24,29)},{rgb(254,229,217), rgb(252,174,145), rgb(251,106,74), rgb(222,45,38), rgb(165,15,21)},{rgb(254,229,217), rgb(252,187,161), rgb(252,146,114), rgb(251,106,74), rgb(222,45,38), rgb(165,15,21)},{rgb(254,229,217), rgb(252,187,161), rgb(252,146,114), rgb(251,106,74), rgb(239,59,44), rgb(203,24,29), rgb(153,0,13)},{rgb(255,245,240), rgb(254,224,210), rgb(252,187,161), rgb(252,146,114), rgb(251,106,74), rgb(239,59,44), rgb(203,24,29), rgb(153,0,13)},{rgb(255,245,240), rgb(254,224,210), rgb(252,187,161), rgb(252,146,114), rgb(251,106,74), rgb(239,59,44), rgb(203,24,29), rgb(165,15,21), rgb(103,0,13)}}, "seq");
	public static ColourScheme RdPu = new ColourScheme("RdPu", new Colour[][]{{rgb(253,224,221), rgb(250,159,181), rgb(197,27,138)},{rgb(254,235,226), rgb(251,180,185), rgb(247,104,161), rgb(174,1,126)},{rgb(254,235,226), rgb(251,180,185), rgb(247,104,161), rgb(197,27,138), rgb(122,1,119)},{rgb(254,235,226), rgb(252,197,192), rgb(250,159,181), rgb(247,104,161), rgb(197,27,138), rgb(122,1,119)},{rgb(254,235,226), rgb(252,197,192), rgb(250,159,181), rgb(247,104,161), rgb(221,52,151), rgb(174,1,126), rgb(122,1,119)},{rgb(255,247,243), rgb(253,224,221), rgb(252,197,192), rgb(250,159,181), rgb(247,104,161), rgb(221,52,151), rgb(174,1,126), rgb(122,1,119)},{rgb(255,247,243), rgb(253,224,221), rgb(252,197,192), rgb(250,159,181), rgb(247,104,161), rgb(221,52,151), rgb(174,1,126), rgb(122,1,119), rgb(73,0,106)}}, "seq");
	public static ColourScheme Greens = new ColourScheme("Greens", new Colour[][]{{rgb(229,245,224), rgb(161,217,155), rgb(49,163,84)},{rgb(237,248,233), rgb(186,228,179), rgb(116,196,118), rgb(35,139,69)},{rgb(237,248,233), rgb(186,228,179), rgb(116,196,118), rgb(49,163,84), rgb(0,109,44)},{rgb(237,248,233), rgb(199,233,192), rgb(161,217,155), rgb(116,196,118), rgb(49,163,84), rgb(0,109,44)},{rgb(237,248,233), rgb(199,233,192), rgb(161,217,155), rgb(116,196,118), rgb(65,171,93), rgb(35,139,69), rgb(0,90,50)},{rgb(247,252,245), rgb(229,245,224), rgb(199,233,192), rgb(161,217,155), rgb(116,196,118), rgb(65,171,93), rgb(35,139,69), rgb(0,90,50)},{rgb(247,252,245), rgb(229,245,224), rgb(199,233,192), rgb(161,217,155), rgb(116,196,118), rgb(65,171,93), rgb(35,139,69), rgb(0,109,44), rgb(0,68,27)}}, "seq");
	public static ColourScheme YlGnBu = new ColourScheme("YlGnBu", new Colour[][]{{rgb(237,248,177), rgb(127,205,187), rgb(44,127,184)},{rgb(255,255,204), rgb(161,218,180), rgb(65,182,196), rgb(34,94,168)},{rgb(255,255,204), rgb(161,218,180), rgb(65,182,196), rgb(44,127,184), rgb(37,52,148)},{rgb(255,255,204), rgb(199,233,180), rgb(127,205,187), rgb(65,182,196), rgb(44,127,184), rgb(37,52,148)},{rgb(255,255,204), rgb(199,233,180), rgb(127,205,187), rgb(65,182,196), rgb(29,145,192), rgb(34,94,168), rgb(12,44,132)},{rgb(255,255,217), rgb(237,248,177), rgb(199,233,180), rgb(127,205,187), rgb(65,182,196), rgb(29,145,192), rgb(34,94,168), rgb(12,44,132)},{rgb(255,255,217), rgb(237,248,177), rgb(199,233,180), rgb(127,205,187), rgb(65,182,196), rgb(29,145,192), rgb(34,94,168), rgb(37,52,148), rgb(8,29,88)}}, "seq");
	public static ColourScheme Purples = new ColourScheme("Purples", new Colour[][]{{rgb(239,237,245), rgb(188,189,220), rgb(117,107,177)},{rgb(242,240,247), rgb(203,201,226), rgb(158,154,200), rgb(106,81,163)},{rgb(242,240,247), rgb(203,201,226), rgb(158,154,200), rgb(117,107,177), rgb(84,39,143)},{rgb(242,240,247), rgb(218,218,235), rgb(188,189,220), rgb(158,154,200), rgb(117,107,177), rgb(84,39,143)},{rgb(242,240,247), rgb(218,218,235), rgb(188,189,220), rgb(158,154,200), rgb(128,125,186), rgb(106,81,163), rgb(74,20,134)},{rgb(252,251,253), rgb(239,237,245), rgb(218,218,235), rgb(188,189,220), rgb(158,154,200), rgb(128,125,186), rgb(106,81,163), rgb(74,20,134)},{rgb(252,251,253), rgb(239,237,245), rgb(218,218,235), rgb(188,189,220), rgb(158,154,200), rgb(128,125,186), rgb(106,81,163), rgb(84,39,143), rgb(63,0,125)}}, "seq");
	public static ColourScheme GnBu = new ColourScheme("GnBu", new Colour[][]{{rgb(224,243,219), rgb(168,221,181), rgb(67,162,202)},{rgb(240,249,232), rgb(186,228,188), rgb(123,204,196), rgb(43,140,190)},{rgb(240,249,232), rgb(186,228,188), rgb(123,204,196), rgb(67,162,202), rgb(8,104,172)},{rgb(240,249,232), rgb(204,235,197), rgb(168,221,181), rgb(123,204,196), rgb(67,162,202), rgb(8,104,172)},{rgb(240,249,232), rgb(204,235,197), rgb(168,221,181), rgb(123,204,196), rgb(78,179,211), rgb(43,140,190), rgb(8,88,158)},{rgb(247,252,240), rgb(224,243,219), rgb(204,235,197), rgb(168,221,181), rgb(123,204,196), rgb(78,179,211), rgb(43,140,190), rgb(8,88,158)},{rgb(247,252,240), rgb(224,243,219), rgb(204,235,197), rgb(168,221,181), rgb(123,204,196), rgb(78,179,211), rgb(43,140,190), rgb(8,104,172), rgb(8,64,129)}}, "seq");
	public static ColourScheme Greys = new ColourScheme("Greys", new Colour[][]{{rgb(240,240,240), rgb(189,189,189), rgb(99,99,99)},{rgb(247,247,247), rgb(204,204,204), rgb(150,150,150), rgb(82,82,82)},{rgb(247,247,247), rgb(204,204,204), rgb(150,150,150), rgb(99,99,99), rgb(37,37,37)},{rgb(247,247,247), rgb(217,217,217), rgb(189,189,189), rgb(150,150,150), rgb(99,99,99), rgb(37,37,37)},{rgb(247,247,247), rgb(217,217,217), rgb(189,189,189), rgb(150,150,150), rgb(115,115,115), rgb(82,82,82), rgb(37,37,37)},{rgb(255,255,255), rgb(240,240,240), rgb(217,217,217), rgb(189,189,189), rgb(150,150,150), rgb(115,115,115), rgb(82,82,82), rgb(37,37,37)},{rgb(255,255,255), rgb(240,240,240), rgb(217,217,217), rgb(189,189,189), rgb(150,150,150), rgb(115,115,115), rgb(82,82,82), rgb(37,37,37), rgb(0,0,0)}}, "seq");
	public static ColourScheme YlOrRd = new ColourScheme("YlOrRd", new Colour[][]{{rgb(255,237,160), rgb(254,178,76), rgb(240,59,32)},{rgb(255,255,178), rgb(254,204,92), rgb(253,141,60), rgb(227,26,28)},{rgb(255,255,178), rgb(254,204,92), rgb(253,141,60), rgb(240,59,32), rgb(189,0,38)},{rgb(255,255,178), rgb(254,217,118), rgb(254,178,76), rgb(253,141,60), rgb(240,59,32), rgb(189,0,38)},{rgb(255,255,178), rgb(254,217,118), rgb(254,178,76), rgb(253,141,60), rgb(252,78,42), rgb(227,26,28), rgb(177,0,38)},{rgb(255,255,204), rgb(255,237,160), rgb(254,217,118), rgb(254,178,76), rgb(253,141,60), rgb(252,78,42), rgb(227,26,28), rgb(177,0,38)}}, "seq");
	public static ColourScheme PuRd = new ColourScheme("PuRd", new Colour[][]{{rgb(231,225,239), rgb(201,148,199), rgb(221,28,119)},{rgb(241,238,246), rgb(215,181,216), rgb(223,101,176), rgb(206,18,86)},{rgb(241,238,246), rgb(215,181,216), rgb(223,101,176), rgb(221,28,119), rgb(152,0,67)},{rgb(241,238,246), rgb(212,185,218), rgb(201,148,199), rgb(223,101,176), rgb(221,28,119), rgb(152,0,67)},{rgb(241,238,246), rgb(212,185,218), rgb(201,148,199), rgb(223,101,176), rgb(231,41,138), rgb(206,18,86), rgb(145,0,63)},{rgb(247,244,249), rgb(231,225,239), rgb(212,185,218), rgb(201,148,199), rgb(223,101,176), rgb(231,41,138), rgb(206,18,86), rgb(145,0,63)},{rgb(247,244,249), rgb(231,225,239), rgb(212,185,218), rgb(201,148,199), rgb(223,101,176), rgb(231,41,138), rgb(206,18,86), rgb(152,0,67), rgb(103,0,31)}}, "seq");
	public static ColourScheme Blues = new ColourScheme("Blues", new Colour[][]{{rgb(222,235,247), rgb(158,202,225), rgb(49,130,189)},{rgb(239,243,255), rgb(189,215,231), rgb(107,174,214), rgb(33,113,181)},{rgb(239,243,255), rgb(189,215,231), rgb(107,174,214), rgb(49,130,189), rgb(8,81,156)},{rgb(239,243,255), rgb(198,219,239), rgb(158,202,225), rgb(107,174,214), rgb(49,130,189), rgb(8,81,156)},{rgb(239,243,255), rgb(198,219,239), rgb(158,202,225), rgb(107,174,214), rgb(66,146,198), rgb(33,113,181), rgb(8,69,148)},{rgb(247,251,255), rgb(222,235,247), rgb(198,219,239), rgb(158,202,225), rgb(107,174,214), rgb(66,146,198), rgb(33,113,181), rgb(8,69,148)},{rgb(247,251,255), rgb(222,235,247), rgb(198,219,239), rgb(158,202,225), rgb(107,174,214), rgb(66,146,198), rgb(33,113,181), rgb(8,81,156), rgb(8,48,107)}}, "seq");
	public static ColourScheme PuBuGn = new ColourScheme("PuBuGn", new Colour[][]{{rgb(236,226,240), rgb(166,189,219), rgb(28,144,153)},{rgb(246,239,247), rgb(189,201,225), rgb(103,169,207), rgb(2,129,138)},{rgb(246,239,247), rgb(189,201,225), rgb(103,169,207), rgb(28,144,153), rgb(1,108,89)},{rgb(246,239,247), rgb(208,209,230), rgb(166,189,219), rgb(103,169,207), rgb(28,144,153), rgb(1,108,89)},{rgb(246,239,247), rgb(208,209,230), rgb(166,189,219), rgb(103,169,207), rgb(54,144,192), rgb(2,129,138), rgb(1,100,80)},{rgb(255,247,251), rgb(236,226,240), rgb(208,209,230), rgb(166,189,219), rgb(103,169,207), rgb(54,144,192), rgb(2,129,138), rgb(1,100,80)},{rgb(255,247,251), rgb(236,226,240), rgb(208,209,230), rgb(166,189,219), rgb(103,169,207), rgb(54,144,192), rgb(2,129,138), rgb(1,108,89), rgb(1,70,54)}}, "seq"); 

	public static ColourScheme RedWhiteGreen = new ColourScheme("rwg", new Colour[][]{{rgb(255,0,0),rgb(255,255,255),rgb(0,255,0)}}, "div");
	
	private static List<ColourScheme> sequence = Arrays.asList(
		Blues,Greens,Oranges,Reds,Purples,Greys	
	);
	
	private static List<ColourScheme> sequence2 = Arrays.asList(
		YlGn, RdPu, PuBu, OrRd, GnBu, BuPu 
	);
	
	private static List<ColourScheme> sequence3 = Arrays.asList(
		RdYlGn, YlOrBr, PuBuGn, YlGnBu, RdYlBu, YlOrRd 
	);
	
	public static ColourScheme sequential(int number) {
		return sequence.get(number % sequence.size());
	}
	
	public static ColourScheme sequential2(int number) {
		return sequence2.get(number % sequence2.size());
	}
	
	public static ColourScheme sequential3(int number) {
		return sequence3.get(number % sequence2.size());
	}
	
	public static class Colour {
		int r; int g; int b; int a;
		public Colour(int r, int g, int b) {
			this.r = r; this.g= g; this.b=b; this.a = 255;
		}
		public Colour(int r, int g, int b, int a) {
			this.r = r; this.g= g; this.b=b; this.a=a;
		}
		public String toHex() {
			return "#"+hex(r)+hex(g)+hex(b);
		}
		private static String hex(int i) {
			return (i<16 ? "0" : "")+Integer.toHexString(i);
		}
		public Color toAwt() {
			return new Color(r,g,b,a);
		}
		private Colour darken(float fraction) {
			r = (int) ((1-fraction)*r);
			g = (int) ((1-fraction)*g);
			b = (int) ((1-fraction)*b);
			return this;
		}
		private Colour lighten(float fraction) {
			r = (int) ((1-fraction)*r+fraction*255);
			g = (int) ((1-fraction)*g+fraction*255);
			b = (int) ((1-fraction)*b+fraction*255);
			return this;
		}
		public void contrast(float minusOneToOne) {
			
			double f = (259*(255*minusOneToOne+255))/(255*(259-255*minusOneToOne));
			
			r = trunc((int) f*(r-128)+128);
			g = trunc((int) f*(g-128)+128);
			b = trunc((int) f*(b-128)+128);
			
			r = r<0 ? 0 : r;
			g = g<0 ? 0 : g;
			b = b<0 ? 0 : b;
			
		}
	}
	
	private static int trunc(int rbg) {
		if (rbg<0) return 0;
		if (rbg>255) return 255;
		return rbg;
	}
	
	private static Colour rgb(int r, int g, int b) {
		return new Colour(r,g,b);
	}
	
	private static Colour rgba(int r, int g, int b, int a) {
		return new Colour(r,g,b,a);
	}
	
	
	
	String name;
	Colour[][] values;
	String usage;
	Colour background;
	
	public ColourScheme(String name, Colour[][] values, String usage) {
		this(name,  rgba(0,0,0,0), values, usage);
	}
	
	public ColourScheme(ColourScheme copy) {
		this(copy.name,  copy.background, copy.values, copy.usage);
	}
	
	public ColourScheme(String name, Colour background, Colour[][] values, String usage) {
		this.name = name;
		this.values = values;
		this.usage = usage;
		this.background = background;
	}
	
	public List<Colour> values(int size) {
		for (Colour[] out: values)
			if (out.length >= size) return Arrays.asList(out);
		Colour[] out = new Colour[size];
		for (int i=0; i<size; i++) {
			double zeroToOne = ((double) i)/(size-1);
			out[i] = continuous(zeroToOne);
		}
		return Arrays.asList(out);
	}

	public Colour background() {
		return background;
	}
	
	public ColourScheme contrast(float minusOneToOne) {
		ColourScheme out = new ColourScheme(this);
		for (int i = 0; i<out.values.length; i++) {
			for (int j = 0; j<out.values[i].length; j++) {
				out.values[i][j].contrast(minusOneToOne);
			}
		}
		return out;
	}
		
	public ColourScheme darker(float ratio) {
		ColourScheme out = new ColourScheme(this);
		for (int i = 0; i<out.values.length; i++) {
			for (int j = 0; j<out.values[i].length; j++) {
				out.values[i][j].darken(ratio);
			}
		}
		return out;
	}
	
	public ColourScheme lighter(float ratio) {
		ColourScheme out = new ColourScheme(this);
		for (int i = 0; i<out.values.length; i++) {
			for (int j = 0; j<out.values[i].length; j++) {
				out.values[i][j].lighten(ratio);
			}
		}
		return out;
	}
	
	public Colour continuous(double zeroToOne) {
		Colour[] tmp = values[values.length-1];
		double position = zeroToOne*(tmp.length-1);
		int index = (int) position;
		double remainder = position-Math.floor(position);
		if (remainder == 0) return tmp[index];
		return interp(tmp[index],tmp[index+1],remainder);
	}
	
	private Colour interp(Colour col1, Colour col2, double frac) {
		return new Colour(
				(int) (col1.r*(1-frac)+col2.r*frac),
				(int) (col1.g*(1-frac)+col2.g*frac),
				(int) (col1.b*(1-frac)+col2.b*frac)
				);
	}
	
	public String getGnuplotPalette(int categories) {
		StringBuilder styles = new StringBuilder("# line styles\n");
		StringBuilder palette = new StringBuilder("# palette\n set palette defined ( \\\n");
		List<Colour> cols = values(categories);
		for (int i=0; i<cols.size(); i++) {
			String col = cols.get(i).toHex();
			styles.append("set style line "+(i+1)+" lt 1 lc rgb '"+col+"'\n");
			if (i>0) palette.append(",\\\n"); 
			palette.append(""+i+" \""+col+"\"");
		};
		return styles.toString()+"\n"+palette.toString()+")\n";
	}
	
	public String getGGplotColourContinuous(String string) {
		List<Colour> cols = values(3);
		return "scale_colour_gradient2(name = \""+string+
				"\", low = \""+cols.get(0).toHex()+
				"\", mid = \""+cols.get(1).toHex()+
				"\", high = \""+cols.get(2).toHex()+"\")";
	}
	
	public String getGGplotFillContinuous(String string, String string2, String string3) {
		List<Colour> cols = values(3);
		return "scale_fill_gradient2(name = \""+string+
				"\", low = \""+cols.get(0).toHex()+
				"\", mid = \""+cols.get(1).toHex()+
				"\", high = \""+cols.get(2).toHex()+"\""+
				(string2 != null ? ", limits=c("+string2+","+string3+")": "")
				+" )";
	}
	
	public String getName() {return name;}
	
}
