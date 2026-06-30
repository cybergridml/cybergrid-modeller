import ScalarValues::*;
import ISQBase::*;
import ISQ::*;
import SI::*;
import NACREEnum::*;
import NACREDatatypes::*;
import NACREProfile::*;
import CRA::*;
import CFA::*;

package CyberGridML2 {
	
	centralcontroller1 :CentralController;
	maingrid1 :MainGrid
	{
	S1 :Substation
	{
		 B1:Bus {
		 	::> nominalLevel {
		 		 unit= "VoltageUnitKind::KV";
		 		::> precision = 135  ;
		 		}
		 		
		 	::> connected= true;
		 	equipment {
		 		B1_G : Generator;
		 		L1_2_1_1 :Terminal;
		 		L1_5_1 :Terminal;
		 	}
		}
	}
	S2 :Substation
	{
		B2:Bus {
			nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
			connected= true;
			equipment {
		 		B1_G : Generator;
		 		L1_2_1_1 :Terminal;
		 		L1_5_1 :Terminal;
		 	}
		
		}
	}
	 S3 :Substation
	{
		B3:Bus {
			nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
			connected= true;	
		
		}
	}
	 S4 : Substation
	{
		B4:Bus {
		nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;		
		}
		B7:Bus {
		nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;		
		}
		B9:Bus {
		nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;		
		}
	}
	 S5 :Substation
	{
		B5:Bus {
		nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;		
		}
		B6:Bus {
		nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;		
		}
	}
	 S8 : Substation
	{
		B8:Bus {
		 nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;		
		}
	}
	 S10 : Substation
	{
		B10:Bus {
		nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;		
		}
	}
	 S11 : Substation
	{
		B11:Bus {
		nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;		
		}
	}
	 S12 : Substation
	{
		B12:Bus {
		 nominalLevel = 12 [VoltageUnitKind::KV];nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;		
		}
	}
	 S13 : Substation
	{
		B13:Bus {
		 nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;connected= true;		
		}
	}
	 S14 : Substation
	{
		B14:Bus {
		nominalLevel {
		 		unit= "VoltageUnitKind::KV";
		 		precision = 135  ;
		 		}
		 connected= true;		
		}
	}

	}
	
	allocate  centralcontroller1 to maingrid1;
}