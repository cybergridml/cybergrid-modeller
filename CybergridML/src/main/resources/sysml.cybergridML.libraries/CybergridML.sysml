library package CyberGridMLEnum{
private import Metaobjects::SemanticMetadata;
private import ScalarValues::*;
private import ISQBase::*;
enum def VoltageUnitKind {
        enum K;
        enum KV;
        enum MV;
    }

	enum def PowerUnitKind {
        enum W;
        enum KW;
        enum MW;
    }
	enum def RedundancyKind {
        enum consensus;
        enum hypervisor;
        enum active_standby;
    }

    enum def EnergyUnitKind {
        enum J;
        enum KJ;
        enum Wh;
        enum KWh;
        enum MWh;
    }

    enum def CurrentUnitKind {
        enum A;
        enum MA;
    }

    enum def ReactivePowerUnitKind {
        enum VAR;
        enum KVAR;
        enum MVAR;
    }

    enum def VoltageLevel {
        enum '20';
        enum '42';
        enum '63';
        enum '90';
        enum '150';
        enum '225';
        enum '380';
    }

    enum def ControlType {
        enum DefaultControl;
        enum MPCRte;
        enum ApproximateLinearMPC;
        enum Limiter;
        enum ReleaseAll;

    }
	enum def LatencyUnitKind {
        enum Nanoseconds;
        enum Microseconds;
        enum Milliseconds;
        enum Seconds;
        enum Minutes;
    }
	enum def CapacityUnitKind {
        enum Bytes;
        enum Kilobytes;
        enum Megabytes;
        enum Gigabytes;
        enum Terabytes;
        enum Petabytes;
    }
      enum def TimeUnitKind{
       enum s;
       enum tick;
       enum ms;
       enum us;
       enum min;
       enum hrs;
       enum day;

    }
    }
library package CyberGridMLDatatypes {
private import Metaobjects::SemanticMetadata;
private import ScalarValues::*;
private import ISQBase::*;
private import CyberGridMLEnum::*;
    attribute def NFP_Latency {
        attribute unit : LatencyUnitKind[0..1] default := Milliseconds;
        attribute precision : Integer[1] default := 30;
    }
    attribute def NFP_Capacity {
        attribute unit : CapacityUnitKind[0..1] default := Kilobytes;
        attribute precision : Integer[1] default := 250;
    }
 s
    attribute def NFP_Voltage {
        attribute unit : VoltageUnitKind[0..1];
        attribute precision : Real[0..1];
    }

    attribute def NFP_Power {
        attribute unit : PowerUnitKind[0..1];
        attribute precision : Real[0..1];
    }

    attribute def NFP_Energy {
        attribute unit : EnergyUnitKind[0..1];
        attribute precision : Real[0..1];
    }

    attribute def NFP_Current {
        attribute unit : CurrentUnitKind[0..1];
        attribute precision : Real[0..1];
    }

    attribute def NFP_ReactivePower {
        attribute unit : ReactivePowerUnitKind[0..1];
        attribute precision : Real;
    }

    attribute def Period {
        attribute start : Integer default := 0;
        attribute finish : Integer default := 15;
    }
	metadata def Hazard {
	attribute description : String;
	attribute justification : String;
	attribute period : Period[1];
	attribute percent : Real[0..1];
	}


}
library package CyberGridMLProfile {
library package CRA {

private import ScalarValues::*;
private import ISQBase::*;
private import CyberGridMLDatatypes::*;
private import CyberGridMLEnum::*;

    part def ComputingResource;
    part def RedundancyCluster {
	    attribute runtime : RedundancyKind[1];
	    ref active : SoftwareResource[1..*];
	    ref passive : SoftwareResource[1..*];
	    ref hypervisor : SoftwareResource[0..1];

	}
    part def CommunicationMedia {
        attribute capacity : NFP_Capacity ;
        attribute latency : NFP_Latency ;
    }

    part def SoftwareResource {
        attribute Activation_period:Period;
        attribute Execution_delay:Period;
    }

    allocation def Allocate {
        end computingRes : ComputingResource;
        end softwareRes : SoftwareResource;
    }

    allocation def Connect {
        end computingRes : ComputingResource;
        end comMedia : CommunicationMedia;
    }

    allocation def Link {
        end source_comMedia : CommunicationMedia;
        end target_comMedia : CommunicationMedia;
    }
}
library package CFA {
private import ScalarValues::*;
private import ISQBase::*;
private import CyberGridMLDatatypes::*;


    part def GridElement;

    allocation def Control {
        end zone : GridElement;
        end controller : Controller;
    }
    part def MainGrid :> GridElement {
        substation : Substation[1..*];
        centralController : CentralController;
		part zone : Zone[1..*];
        part line : Line[1..*];

    }

    part def Zone :> GridElement {

         part zonalController : ZonalController;
		 part substations : Substation[1..*];
		 part line : Line[1..*];

    }


    part def Substation {
         part bus : Bus[1..*];
		 ref zone : Zone[*];

    }

    part def Bus {
        attribute nominalLevel : NFP_Voltage;
		attribute connected : Boolean;
        part equipments : Equipment[1..*];
        ref substation : Substation[1];
    }

    part def Controller {
        attribute controlBehavior : String;
    }

    part def CentralController :> Controller
	{
	ref maingrid : MainGrid[1];
    }
    part def LocalController :> Controller;

    part def ZonalController :> Controller
	{
	ref zone : Zone[1];
	}

    part def Equipment {
        localController : LocalController;
		attribute connected : Boolean;
        attribute p : NFP_Power;
        attribute q : NFP_Power;
        attribute activable  : Boolean;
    }

    part def Generator :> Equipment;

    part def Battery :> Equipment;
    part def Load :> Equipment;

    part def Line{
	ref maingrid : MainGrid[1];
	ref terminal : Terminal[0..1];
	}
    part def Terminal :> Equipment
	{
	    attribute finish : String;
		attribute branch : String;
        attribute side : Integer;
        attribute ecr : NFP_Current;
		ref line : Line[0..2];
		attribute v : NFP_Voltage;
	}


}
}
