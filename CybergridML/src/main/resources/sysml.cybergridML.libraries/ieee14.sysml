import ScalarValues::*;
import ISQBase::*;
import ISQ::*;
import SI::*;
import NACREEnum::*;
import CyberGridMLDatatypes::*;
import CyberGridMLProfile::*;
import CRA::*;
import CFA::*;

package CyberGridML2 {
	
	centralcontroller1 :CentralController;
	maingrid1 :MainGrid
	{
	S1 :Substation
	{
		 B1:Bus {
		 	
		 		B1_G : Generator;
		 		L1_2_1_1 :Terminal;
		 		L1_5_1 :Terminal;
		 	
		 }		 			
	}
	S2 :Substation
	{
		B2:Bus {		
			
		 		B2_G : Generator;
		 		B2_L: Load;
		 		L1_2_1_1 :Terminal;
		 		L2_3_1_1 :Terminal;
		 		L2_4_1_1 :Terminal;
		 		L2_5_1_1 :Terminal;
				
		}
	}
	 S3 :Substation
	{
		B3:Bus {
				B3_G : Generator;
		 		B3_L: Load;
		 		L2_3_1_2 :Terminal;
		 		L3_4_1_1 :Terminal;
		
		}
	}
	 S4 : Substation
	{
		B4:Bus {
		B4_L: Load;
		 		L2_4_1_2 :Terminal;
		 		L3_4_1_2 :Terminal;
		 		L4_5_1_1 :Terminal;
		 		T4_7_1_1 :Terminal;
		T4_9_1_1 :Terminal;
		}
		B7:Bus {
		L7_8_1_1 :Terminal;
		 		L7_9_1_1 :Terminal;
		 		T4_7_1_2 :Terminal;
		 	
		}
		B9:Bus {
		B9_L: Load;
		 		L7_9_1_2 :Terminal;
		 		L9_10_1_1 :Terminal;
		 		L9_14_1_1 :Terminal;
		 		T4_9_1_2 :Terminal;
		 	
		}
	}
	 S5 :Substation
	{
		B5:Bus {
		B5_L: Load;
		 		L1_5_1_2 :Terminal;
		 		L2_5_1_2 :Terminal;
		 		L4_5_1_2 :Terminal;
		 		T5_6_1_1 :Terminal;
			
		}
		B6:Bus {
		B6_G : Generator;
		 		B6_L: Load;
		 		L6_11_1_1 :Terminal;
		 		L6_12_1_1 :Terminal;
		 		L6_13_1_1 :Terminal;
		 		T5_6_1_2 :Terminal;
		
		}
	}
	 S8 : Substation
	{
		B8:Bus {
				B8_G : Generator;
				 L7_8_1_2 :Terminal;
		}
	}
	 S10 : Substation
	{
		B10:Bus {
				B10_L: Load;
		 		L9_10_1_2:Terminal;
		 		L10_11_1_1 :Terminal;
		 		
		}
	}
	 S11 : Substation
	{
		B11:Bus {
				B11_L: Load;
		 		L6_11_1_2:Terminal;
		 		L10_11_1_2 :Terminal;
			
		}
	}
	 S12 : Substation
	{
		B12:Bus {
		 		B12_L: Load;
		 		L6_11_1_2:Terminal;
		 		L12_13_1_1 :Terminal;
				
		}
	}
	 S13 : Substation
	{
		B13:Bus {
				B13_L: Load;
		 		L6_13_1_2:Terminal;
		 		L12_13_1_2 :Terminal;
				L13_14_1_1 :Terminal;
		
		}
	}
	 S14 : Substation
	{
		B14:Bus {
				B14_L: Load;
		 		L9_14_1_2:Terminal;
				L13_14_1_2 :Terminal;
				 		
		}
	}

	}
	
	allocate  centralcontroller1 to maingrid1;
}