using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.AI;
using UnityEngine.UI;
using UnityEngine.Animations;

public class control : MonoBehaviour
{
    public NavMeshAgent agent;
    public PositionConstraint posConstr;
    public float stepDistance = 0.5f; 
    public float rotationAngle = 45.0f; 

    //moves of one step
    public void MoveForward()
    {
        if(posConstr.constraintActive)
            posConstr.constraintActive = false;
        Vector3 newPosition = transform.position + transform.forward * stepDistance;
        //Checks if an obstacle is in front of the turtle 
        if (Physics.Raycast(transform.position, transform.forward, out RaycastHit hitInfo, stepDistance))
        {
            if (hitInfo.collider.gameObject.CompareTag("Wall"))
            {
                Debug.Log("Wall detected. Can't move forward.");
                return;
            }
        }
        //Checks if the new position lies on the NavMesh 
        if (NavMesh.SamplePosition(newPosition, out NavMeshHit hit, 1.0f, NavMesh.AllAreas))
        {
            //Update the position
            agent.Warp(hit.position);
            
        }
    }

    public void RotateLeft()
    {
        transform.Rotate(Vector3.up, -rotationAngle);//45 degrees
        
    }

    public void RotateRight()
    {
        transform.Rotate(Vector3.up, rotationAngle);//45 degrees
        
    }


}
