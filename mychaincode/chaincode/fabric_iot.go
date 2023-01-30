package chaincode

import (
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// SmartContract provPhonees functions for managing an Device
type SmartContract struct {
	contractapi.Contract
}

// Device describes basic details of what makes up a simple device
// Device
type Device struct {
	MAC				string `json:"macaddress"`
	DeviceName		string `json:"devicename"`
}

// InitLedger adds a base set of devices to the ledger
func (s *SmartContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	devices := []Device{
		{MAC: "aa:bb:cc:dd:ee:ff", DeviceName: "SM-V510"}, 
		{MAC: "aa:bb:cc:dd:ee:gg", DeviceName: "SM-R220"}, 
		{MAC: "aa:bb:cc:dd:ee:hh", DeviceName: "NT761XDZ-G78A"}, 
	}

	for _, device := range devices {
		deviceJSON, err := json.Marshal(device)
		if err != nil {
			return err
		}

		keyvalue = device.MAC + device.Phone

		err = ctx.GetStub().PutState(keyvalue, deviceJSON)
		if err != nil {
			return fmt.Errorf("failed to put to world state. %v", err)
		}
	}

	return nil
}

// CreateDevice issues a new device to the world state with given details.
func (s *SmartContract) CreateDevice(ctx contractapi.TransactionContextInterface, rawDeviceEnrollRequest string) (string, error) {

	var deviceEnrollRequest Device
	err := json.Unmarshal([]byte(rawDeviceEnrollRequest), &deviceEnrollRequest)
	if err != nil {
		return "", err
	}

	keyvalue = deviceEnrollRequest.MAC + deviceEnrollRequest.DeviceName

	exists, err := s.DeviceExists(ctx, keyvalue)
	if err != nil {
		return "", err
	}
	if exists {
		return "", fmt.Errorf("the device %s already exists", keyvalue)
	}

	result, err := json.Marshal(deviceEnrollRequest)
	if err != nil {
		return "", fmt.Errorf("failed json.Marshal: %v", err)
	}

	err = ctx.GetStub().PutState(keyvalue, result)
	if err != nil {
		return "", err
	}

	return "CreateDevice OK", nil
}

// ReadDevice returns the device stored in the world state with given Phone.
func (s *SmartContract) ReadDevice(ctx contractapi.TransactionContextInterface, rawDeviceReadRequest string) (*Device, error) {
	var deviceReadRequest Device
	err := json.Unmarshal([]byte(rawDeviceReadRequest), &deviceReadRequest)
	if err != nil {
		return nil, err
	}

	keyvalue = deviceEnrollRequest.MAC + deviceEnrollRequest.DeviceName

	deviceJSON, err := ctx.GetStub().GetState(keyvalue)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if deviceJSON == nil {
		return nil, fmt.Errorf("the device %s does not exist", keyvalue)
	}

	var device Device
	err = json.Unmarshal(deviceJSON, &device)
	if err != nil {
		return nil, err
	}

	return &device, nil
}

// UpdateDevice updates an existing device in the world state with provPhoneed parameters.
func (s *SmartContract) UpdateDevice(ctx contractapi.TransactionContextInterface, rawDeviceUpdateRequest string) (string, error) {

	var deviceUpdateRequest Device
	err := json.Unmarshal([]byte(rawDeviceUpdateRequest), &deviceUpdateRequest)
	if err != nil {
		return "", err
	}

	keyvalue = deviceEnrollRequest.MAC + deviceUpdateRequest.DeviceName

	exists, err := s.DeviceExists(ctx, keyvalue)
	if err != nil {
		return "", err
	}
	if !exists {
		return "", fmt.Errorf("the device %s does not exist", keyvalue)
	}

	result, err := json.Marshal(deviceUpdateRequest)
	if err != nil {
		return "", fmt.Errorf("failed json.Marshal: %v", err)
	}

	err = ctx.GetStub().PutState(keyvalue, result)
	if err != nil {
		return "", err
	}

	return "UpdateDevice OK", nil
}

// DeleteDevice deletes an given device from the world state.
func (s *SmartContract) DeleteDevice(ctx contractapi.TransactionContextInterface, rawDeviceDeleteRequest string) (string, error) {
	var deviceDeleteRequest Device
	err := json.Unmarshal([]byte(rawDeviceDeleteRequest), &deviceDeleteRequest)
	if err != nil {
		return "", err
	}

	keyvalue = deviceEnrollRequest.MAC + deviceEnrollRequest.DeviceName

	exists, err := s.DeviceExists(ctx, keyvalue)
	if err != nil {
		return "", err
	}
	if !exists {
		return "", fmt.Errorf("the device %s does not exist", keyvalue)
	}

	err = ctx.GetStub().DelState(keyvalue)
	if err != nil {
		return "", err
	}

	return "DeleteDevice OK", nil
}

// DeviceExists returns true when device with given Phone exists in world state
func (s *SmartContract) DeviceExists(ctx contractapi.TransactionContextInterface, keyvalue string) (bool, error) {
	deviceJSON, err := ctx.GetStub().GetState(keyvalue)
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %v", err)
	}

	return deviceJSON != nil, nil
}

// GetAllDevices returns all devices found in world state
func (s *SmartContract) GetAllDevices(ctx contractapi.TransactionContextInterface) ([]*Device, error) {
	// range query with empty string for startKey and endKey does an
	// open-ended query of all devices in the chaincode namespace.
	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	var devices []*Device
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		var device Device
		err = json.Unmarshal(queryResponse.Value, &device)
		if err != nil {
			return nil, err
		}
		devices = append(devices, &device)
	}

	return devices, nil
}
