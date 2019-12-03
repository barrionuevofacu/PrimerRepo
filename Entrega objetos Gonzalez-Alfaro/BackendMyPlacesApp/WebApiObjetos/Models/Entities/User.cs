using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;
using WebApiObjetos.Domain;

namespace WebApiObjetos.Models.Entities
{
    public class User
    {
        public int Id { get; set; }

        public string UserName { get; set; }

        public string Password { get; set; }

        public string Email { get; set; }

        public UserDTO toDto()
        {
            return new UserDTO
            {
                UserName = this.UserName,
                Password = this.Password,
                Email = this.Email,
            };
        }
    }
}
