using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Runtime.Serialization;
using System.Threading.Tasks;
using WebApiObjetos.Models.Entities;

namespace WebApiObjetos.Domain
{
    [DataContract()]
    public class UserDTO
    {

        [DataMember()]
        [Required(ErrorMessage = "UserName cannot be empty")]
        public string UserName { get; set; }

        [DataMember()]
        [Required(ErrorMessage = "PassWord cannot be empty")]
        public string Password { get; set; }

        [DataMember()]
        public string Email { get; set; }

        [DataMember()]
        public string Token { get; set; }
        
        public User ToEntity()
        {
            return new User
            {
                UserName = this.UserName,
                Password = this.Password,
                Email = this.Email,
            };
        }
    }
}
