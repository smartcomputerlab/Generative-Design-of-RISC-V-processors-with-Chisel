# Generative Design of RISC-V Processors with Chisel

## Project Overview

**Generative Design of RISC-V Processors with Chisel** is an open-source hardware design and educational project dedicated to the development, simulation, verification, and implementation of configurable RISC-V processors using **Chisel (Constructing Hardware in a Scala Embedded Language)**. The project explores a modern approach to processor design in which hardware architectures are described through parameterized, reusable, and composable components rather than traditional, manually written Register Transfer Level (RTL) descriptions.

The primary objective is to demonstrate how **hardware generation techniques** can accelerate the design and exploration of RISC-V processor architectures while maintaining clarity, modularity, and compatibility with established digital design workflows.

### RISC-V Processor Architecture

The project follows an incremental design methodology based on the RISC-V Instruction Set Architecture (ISA), progressing through three principal configurations:

- **RV32I:** A 32-bit integer processor implementing the fundamental RISC-V instruction set, including arithmetic, logical, load/store, branch, jump, and system instructions.
- **RV32IM:** An extension of the RV32I architecture incorporating hardware multiplication and division through the RISC-V M extension.
- **RV32IMF:** A further extension integrating single-precision floating-point operations through the RISC-V F extension, including floating-point arithmetic, conversions, comparisons, and related control functionality.

This progressive development illustrates how a basic processor can evolve into a more capable computing architecture while preserving a modular design structure.

### Generative Hardware Design with Chisel

The processor architectures are developed using **Chisel 3**, a hardware construction language embedded in Scala. Chisel enables designers to describe digital circuits using high-level programming abstractions while generating synthesizable RTL.

The project emphasizes parameterized hardware components, including the Arithmetic Logic Unit (ALU), register files, instruction decoder, control unit, memory interfaces, multiplication and division units, and Floating-Point Unit (FPU).

By exploiting Scala's programming capabilities, the design methodology supports hardware reuse, architectural customization, automated elaboration, and systematic exploration of alternative processor configurations.

### Simulation, Verification, and Implementation

Verification is an essential component of the project. Individual hardware modules and complete processor configurations are evaluated through simulation and automated testing.

The development workflow integrates several complementary tools and techniques:

- **Scala and ChiselTest:** Functional verification through automated testbenches and instruction-level testing.
- **Verilog generation:** Translation of Chisel hardware descriptions into synthesizable RTL.
- **GTKWave:** Visualization and analysis of simulation waveforms.
- **FPGA synthesis:** Evaluation of hardware resource utilization, timing characteristics, and implementation feasibility.
- **ASIC-oriented synthesis:** Exploration of logic complexity, area, and timing characteristics using standard digital synthesis methodologies.

These activities establish a practical connection between high-level processor modeling and physical hardware implementation.

### Educational and Research Objectives

The repository is intended for students, researchers, educators, and hardware engineers interested in computer architecture, digital systems, open-source processors, and hardware generation methodologies.

It provides a structured framework for understanding the relationship between instruction-set specifications, processor microarchitecture, RTL generation, functional verification, and hardware synthesis.

Beyond implementing the RISC-V instruction set, the project investigates how generative design can improve hardware productivity, facilitate architectural experimentation, and reduce the complexity of developing increasingly sophisticated processor systems.

### Expected Outcomes

The principal outcome is a modular and extensible RISC-V processor design framework that supports the generation and evaluation of different processor configurations, from a basic 32-bit integer core to a processor incorporating multiplication, division, and single-precision floating-point capabilities.

Ultimately, **Generative Design of RISC-V Processors with Chisel** aims to bridge the gap between theoretical computer architecture and practical hardware engineering, providing a reproducible foundation for teaching, experimentation, and future research in open-source processor design.

**Keywords:** RISC-V, RV32I, RV32IM, RV32IMF, Chisel 3, Scala, Generative Hardware Design, Processor Architecture, RTL, Verilog, ChiselTest, GTKWave, FPGA, ASIC, Hardware Verification.
